package com.v1.pension.service.pensioner;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;

import jakarta.persistence.Tuple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.v1.pension.exception.BadRequestException;
import com.v1.pension.exception.ResourceNotFoundException;
import com.v1.pension.model.pensioner.VoucherTransactionDetail;
import com.v1.pension.model.pensioner.VoucherTransactionMaster;
import com.v1.pension.repository.pensioner.PensionerRegistrationRepository;
import com.v1.pension.repository.pensioner.VoucherTransactionDetailRepository;
import com.v1.pension.repository.pensioner.VoucherTransactionMasterRepository;
import com.v1.pension.repository.system.FiscalYearRepository;
import com.v1.pension.repository.system.NepaliCalendarRepository;
import com.v1.pension.utils.CurrentUser;
import com.v1.pension.utils.DbUtils;
import com.v1.pension.utils.MyResponse;

@Service
@RequiredArgsConstructor
public class MonthlyVoucherService {
  private final VoucherTransactionMasterRepository transactionMasterRepository;
  private final VoucherTransactionDetailRepository transactionDetailRepository;
  private final PensionerRegistrationRepository pensionerRegistrationRepository;
  private final NepaliCalendarRepository nepaliCalendarRepository;
  private final FiscalYearRepository fiscalYearRepository;

  private final Logger logger = LoggerFactory.getLogger(MonthlyVoucherService.class);

  public List<Tuple> getMasterList() {
    return transactionMasterRepository.getMonthlyAndIncrementVoucherTransactions(CurrentUser.getFiscalYear());
  }

  public List<Tuple> getDetailsByMaster(long masterId) {
    return transactionDetailRepository.getMonthlyVoucherDetailsByMaster(masterId);
  }

  public VoucherTransactionMaster getMasterById(long id) {
    var master = transactionMasterRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Voucher Transaction Not Found"));
    var month = Arrays.stream(master.getStartDate().split("-")).toList().get(1);
    master.setMonth(Integer.parseInt(month));
    return master;
  }

  public long generateMonthlyPension(VoucherTransactionMaster master) {
    var month = master.getMonth();
    var fy = fiscalYearRepository.findByFiscalYear(CurrentUser.getFiscalYear());
    var year = List.of(1, 2, 3).contains(month) ? fy.getEndYear() : fy.getStartYear();

    var nepDate = nepaliCalendarRepository.findByYearAndMonthAndDay(year, month, 1)
        .orElseThrow(() -> new ResourceNotFoundException("Nepali Date Record Not Found In Database"));
    master.setStartDate(String.format("%d-%02d-01", year, month));
    master.setEndDate(String.format("%d-%02d-%d", year, month, nepDate.getNepaliDaysInMonth()));
    master.setCalculationFromDate(master.getStartDate());
    master.setCalculationToDate(master.getEndDate());

    if (transactionMasterRepository.existsByStartDateAndVoucherType(master.getStartDate(), master.getVoucherType()))
      throw new BadRequestException("Monthly Pension Has Already Been Calculated For This Month");

    master.setVoucherNo(transactionMasterRepository.getNextVoucherNumber(CurrentUser.getFiscalYear()));
    master.setStatus(1);
    master.setStage(DbUtils.getReceiveStageCodeForRole(CurrentUser.getUserRole()));
    master.setFiscalYear(CurrentUser.getFiscalYear());
    master.setOfficeCode(CurrentUser.getOfficeCode());

    transactionMasterRepository.save(master);

    var pensioners = MyResponse.data(pensionerRegistrationRepository
        .getMonthlyPensionInformation(CurrentUser.getFiscalYear(), master.getCalculationFromDate()));
    var details = new ArrayList<VoucherTransactionDetail>();

    var yearMonthCombo = String.format("%d-%02d", year, month);
    for (var p : pensioners) {
      var voucherMasterId = (Long) p.get("voucher_master_id");
      var pensionNumber = (String) p.get("sub_number");
      var endDate = (String) p.get("pension_end_date_nep");

      // if end date has already been passed, we skip
      if (endDate != null && endDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
        var endDateYear = Integer.parseInt(endDate.split("-")[0]);
        var endDateMonth = Integer.parseInt(endDate.split("-")[1]);

        if (endDateYear < year || (endDateYear == year && endDateMonth < month))
          continue;
      }

      // last adjustment voucher consideration
      Long lastVoucherMasterIdBeforeIncrementApplied = 142835620L;
      var voucherCalcFromDate = (String) p.get("calculation_from_date");
      var voucherCalcToDate = (String) p.get("calculation_to_date");
      if (voucherMasterId != null && voucherMasterId > lastVoucherMasterIdBeforeIncrementApplied
          && voucherCalcFromDate != null && voucherCalcFromDate.length() == 10
          && voucherCalcToDate != null && voucherCalcToDate.length() == 10) {
        var calculationFromDatePrefix = voucherCalcFromDate.substring(0, 7);
        var calculationToDatePrefix = voucherCalcToDate.substring(0, 7);

        if (yearMonthCombo.compareTo(calculationFromDatePrefix) >= 0
            && yearMonthCombo.compareTo(calculationToDatePrefix) <= 0) {
          logger.info("Skipped Monthly Voucher Calculation For {} due to voucher adjustment", pensionNumber);
          continue;
        }
      }

      double monthlyAmount = (double) p.get("monthly_amount") + (double) p.get("increment_amount");

      var d = new VoucherTransactionDetail();
      d.setMasterId(master.getId());
      d.setPensionSubPNo(pensionNumber);
      d.setFiscalYear(CurrentUser.getFiscalYear());
      d.setStartDate(master.getStartDate());
      d.setEndDate(master.getEndDate());
      d.setReceivableAmount((int) p.get("festival_month") == month ? monthlyAmount * 2 : monthlyAmount);
      d.setIncrement(0);

      // end-date logic
      if (endDate != null && !endDate.isBlank() && endDate.startsWith(yearMonthCombo)) {
        var endDateEntity = nepaliCalendarRepository.findById(endDate)
            .orElseThrow(() -> new ResourceNotFoundException("Date not found in database"));
        var endDateDayCount = Integer.parseInt(endDate.split("-")[2]);
        var daysInMonth = endDateEntity.getNepaliDaysInMonth();

        var actualReceivable = d.getReceivableAmount() / daysInMonth * endDateDayCount;
        var v1 = BigDecimal.valueOf(actualReceivable).setScale(2, RoundingMode.HALF_UP);
        d.setReceivableAmount(v1.doubleValue());
      }

      d.setTax(0);

      d.setTotalAmount(d.getReceivableAmount() + d.getIncrement() - d.getTax());
      details.add(d);
    }

    transactionDetailRepository.saveAll(details);
    return master.getId();
  }
}
