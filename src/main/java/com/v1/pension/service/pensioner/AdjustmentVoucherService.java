package com.v1.pension.service.pensioner;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

import jakarta.persistence.Tuple;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.v1.pension.dto.pensioner.voucher_transaction.VoucherCalculationDto;
import com.v1.pension.dto.pensioner.voucher_transaction.VoucherTaxCalculationDto;
import com.v1.pension.dto.pensioner.voucher_transaction.YearlyTaxableParametersDto;
import com.v1.pension.exception.BadRequestException;
import com.v1.pension.exception.ResourceNotFoundException;
import com.v1.pension.model.pensioner.VoucherTransactionDetail;
import com.v1.pension.model.pensioner.VoucherTransactionMaster;
import com.v1.pension.model.system.NepaliCalendar;
import com.v1.pension.model.system.PensionIncrement;
import com.v1.pension.repository.pensioner.PensionerRegistrationRepository;
import com.v1.pension.repository.pensioner.VoucherTransactionDetailRepository;
import com.v1.pension.repository.pensioner.VoucherTransactionMasterRepository;
import com.v1.pension.repository.report.PensionStatementReportRepository;
import com.v1.pension.repository.system.NepaliCalendarRepository;
import com.v1.pension.repository.system.PensionIncrementRepository;
import com.v1.pension.utils.CurrentUser;
import com.v1.pension.utils.DbUtils;

@Service
@RequiredArgsConstructor
public class AdjustmentVoucherService {
  private final PensionerRegistrationRepository pensionerRegistrationRepository;
  private final VoucherTransactionMasterRepository masterRepository;
  private final VoucherTransactionDetailRepository detailRepository;
  private final NepaliCalendarRepository nepaliCalendarRepository;
  private final PensionIncrementRepository pensionIncrementRepository;
  private final PensionStatementReportRepository statementReportRepository;

  public VoucherCalculationDto getVoucherInfoById(long id) {
    var dto = new VoucherCalculationDto();
    var master = masterRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Voucher Calculation Not Found"));
    var details = detailRepository.getDetailsByMaster(id);

    dto.setMaster(master);
    dto.setDetails(details);
    return dto;
  }

  public List<Tuple> getAdjustmentTransactionsForPensioner(String subPNo) {
    return masterRepository.getAdjustmentTransactionsForPensioner(subPNo);
  }

  @PreAuthorize("@customSecurityService.hasPermissionOverPensioner(#master.pensionSubPNo)")
  public long saveAdjustmentVoucherTransaction(VoucherTransactionMaster master,
      List<VoucherTransactionDetail> details) {
    var pensioner = pensionerRegistrationRepository.findByPensionSubPNo(master.getPensionSubPNo())
        .orElseThrow(() -> new ResourceNotFoundException("Pensioner Not Found"));
    var isDirectSubPensioner = pensionerRegistrationRepository.isDirectSubPensioner(master.getPensionSubPNo());
    if (!isDirectSubPensioner && pensioner.getPensionDeathDateNep() != null
        && !pensioner.getPensionDeathDateNep().isBlank())
      throw new BadRequestException("मृत्यु भएको |");

    var roleName = CurrentUser.getUserRole();
    var targetStage = DbUtils.getReceiveStageCodeForRole(roleName);

    if (master.getId() == null) {
      master.setVoucherNo(masterRepository.getNextVoucherNumber(CurrentUser.getFiscalYear()));
      master.setStage(targetStage);
      master.setFiscalYear(CurrentUser.getFiscalYear());
      master.setOfficeCode(CurrentUser.getOfficeCode());
    } else {
      var oldMaster = masterRepository.findById(master.getId())
          .orElseThrow(() -> new ResourceNotFoundException("Voucher Not Found"));
      master.setOfficeCode(oldMaster.getOfficeCode());
    }

    master.setVoucherType(1); // 1 = Adjustment
    masterRepository.save(master);

    for (var d : details) {
      d.setMasterId(master.getId());
      if (d.getId() == null || d.getId() == 0)
        d.setFiscalYear(CurrentUser.getFiscalYear());
    }

    detailRepository.saveAll(details);

    return master.getId();
  }

  public double generateTaxForAdjustment(VoucherTaxCalculationDto dto) {
    var pensioner = pensionerRegistrationRepository.findByPensionSubPNo(dto.getPensionSubPNo())
        .orElseThrow(() -> new ResourceNotFoundException("Pensioner Not Found"));

    var subPNo = pensioner.getPensionSubPNo();
    var gender = pensioner.getPensionGender();

    // month = 3 = asar = last month of fiscal year
    var params = new YearlyTaxableParametersDto(
        subPNo,
        pensioner.getOtherTypeCode(),
        pensioner.getPensionMaritalStatus(),
        pensioner.getFestivalMonth(),
        null,
        dto.getAmount(),
        3,
        dto.getMasterId());

    double yearlyTaxableAmount = getYearlyTaxableAmountForAdjustment(params);

    return calculateTaxAmount(subPNo, gender, yearlyTaxableAmount, null, 3, -1L);
  }

  private double calculateTaxAmount(String subPNo, String gender, double yearlyTaxableAmount, Double totalPaidTax,
      int month, Long masterId) {
    double tax = 0;

    if (yearlyTaxableAmount < 0)
      return tax;

    // rule for first 5 lakhs
    if (yearlyTaxableAmount > 0) {
      var taxableAmount = yearlyTaxableAmount > 5_00_000 ? 5_00_000 : yearlyTaxableAmount; // first 5 lakh
      tax += taxableAmount * 0.1; // 10% for first 5 lakh
      yearlyTaxableAmount -= taxableAmount; // update yearly taxable amount
    }

    // rule for next 10 lakhs
    if (yearlyTaxableAmount > 0) {
      var taxableAmount = yearlyTaxableAmount > 10_00_000 ? 10_00_000 : yearlyTaxableAmount; // next 10 lakh
      tax += taxableAmount * 0.2; // 20% for next 10 lakh
      yearlyTaxableAmount -= taxableAmount; // update yearly taxable amount
    }

    // rule for next 15 lakhs
    if (yearlyTaxableAmount > 0) {
      var taxableAmount = yearlyTaxableAmount > 15_00_000 ? 15_00_000 : yearlyTaxableAmount; // next 15 lakh
      tax += taxableAmount * 0.27; // 27% for next 15 lakh
      yearlyTaxableAmount -= taxableAmount; // update yearly taxable amount
    }

    // rule for remaining amount
    if (yearlyTaxableAmount > 0) {
      tax += yearlyTaxableAmount * 0.29; // 29% for rest
    }

    if (totalPaidTax == null) {
      totalPaidTax = detailRepository.getTotalPaidTaxForFy(subPNo, CurrentUser.getFiscalYear());
    }

    if (gender.equals("2"))
      tax = tax - 0.1 * tax; // 10% discount on tax for female

    tax -= totalPaidTax;

    if (tax < 0)
      return 0;

    // var monthName = MonthName.getByCalendarMonth(month);
    // var monthlyTax = tax / (12 - monthName.getFyMonth() + 1);

    DecimalFormat df = new DecimalFormat("#.##");
    return Double.parseDouble(df.format(tax));
  }

  private double getYearlyTaxableAmountForAdjustment(YearlyTaxableParametersDto dto) {
    Double receivedAmount = detailRepository.getTotalReceivableForFy(dto.pensionSubPNo(),
        CurrentUser.getFiscalYear(), dto.masterId());

    boolean isDisable = dto.otherTypeCode().equals("3");
    double taxAdvantage = 10_00_000; // 10 lakh
    taxAdvantage = taxAdvantage + taxAdvantage * 0.25; // pensioner advantage
    if (isDisable)
      taxAdvantage = taxAdvantage + taxAdvantage * 0.5; // disable advantage

    double yearlyAmount = dto.monthlyAmount() + receivedAmount;

    return yearlyAmount - taxAdvantage;
  }

  public VoucherCalculationDto generateVoucherCalculation(VoucherTransactionMaster master) {
    if (master.getStartDate().compareTo(master.getEndDate()) > 0)
      throw new BadRequestException("Start date should be earlier than end date");

    var startDateParts = Arrays.stream(master.getStartDate().split("-")).toList();
    var startYear = Integer.parseInt(startDateParts.get(0));
    var startMonth = Integer.parseInt(startDateParts.get(1));
    var startDay = Integer.parseInt(startDateParts.get(2));

    var endDateParts = Arrays.stream(master.getEndDate().split("-")).toList();
    var endYear = Integer.parseInt(endDateParts.get(0));
    var endMonth = Integer.parseInt(endDateParts.get(1));
    var endDay = Integer.parseInt(endDateParts.get(2));

    var pensioner = pensionerRegistrationRepository.findByPensionSubPNo(master.getPensionSubPNo())
        .orElseThrow(() -> new ResourceNotFoundException("Pensioner not found"));
    var increments = pensionIncrementRepository.findAllBySchemeCodeAndPostCode(master.getSchemeCode(),
        pensioner.getPensionPostCode());
    var pensionAmount = master.getPensionAmount();

    var details = new ArrayList<VoucherTransactionDetail>();
    var group = 1;

    for (int i = startYear; i <= endYear; i++) {
      // month value. starts from 1 for non-start year
      var m = i == startYear ? startMonth : 1;
      for (int j = m; j <= 12; j++) {
        NepaliCalendar nepaliDate;
        if (i == startYear && j == startMonth) {
          nepaliDate = nepaliCalendarRepository.findByYearAndMonthAndDay(i, j, startDay)
              .orElseThrow(() -> new ResourceNotFoundException("Nepali Date Record Not Found In Database"));
        } else {
          nepaliDate = nepaliCalendarRepository.findByYearAndMonthAndDay(i, j, 1)
              .orElseThrow(() -> new ResourceNotFoundException("Nepali Date Record Not Found In Database"));
        }
        var incrementAmount = getIncrementAmount(nepaliDate, increments, pensioner.getPensionIncrementPercentage());
        pensionAmount += incrementAmount;
        var perDayRate = pensionAmount / nepaliDate.getNepaliDaysInMonth();

        if (startYear == endYear && startMonth == endMonth) {
          // same year, same month: calculate and break (no more loop needed)
          var period = endDay - startDay + 1;
          var amount = perDayRate * period;
          var detail = generateDetail(master.getStartDate(), master.getEndDate(), period, 1, amount, incrementAmount,
              group);
          details.add(detail);
          break;
        } else if (i == startYear && j == startMonth) {
          // start month
          var endDate = formatDate(startYear, startMonth, nepaliDate.getNepaliDaysInMonth());
          var period = nepaliDate.getNepaliDaysInMonth() - startDay + 1;
          var amount = perDayRate * period;
          var detail = generateDetail(master.getStartDate(), endDate, period, 1, amount, incrementAmount, group);
          details.add(detail);

          group++; // increment for next month
        } else if (i == endYear && j == endMonth) {
          // end month
          group++; // we don't want to group it with others

          var startDate = formatDate(endYear, endMonth, 1);
          var amount = perDayRate * endDay;
          var detail = generateDetail(startDate, master.getEndDate(), endDay, 1, amount, incrementAmount, group);
          details.add(detail);
        } else {
          // rest of the months
          if (incrementAmount > 0)
            group++;

          var startDate = formatDate(i, j, 1);
          var endDate = formatDate(i, j, nepaliDate.getNepaliDaysInMonth());
          var detail = generateDetail(startDate, endDate, 1, 2, pensionAmount, incrementAmount, group);
          details.add(detail);

          if (pensioner.getFestivalMonth() == j) {
            var festiveDetail = generateDetail(startDate, endDate, 1, 2, pensionAmount, 0, group);
            details.add(festiveDetail);
          }
        }

        // stoppage condition
        if (i == endYear && j == endMonth)
          break;
      }
    }

    var nextStartDayOpt = nepaliCalendarRepository.getNextDay(master.getEndDate());
    var responseDto = new VoucherCalculationDto();
    master.setPensionAmount(pensionAmount);
    if (!master.isContinued() || master.getCalculationFromDate().isBlank())
      master.setCalculationFromDate(master.getStartDate());
    master.setCalculationToDate(master.getEndDate());
    master.setNewPensionAmount(pensionAmount);
    nextStartDayOpt.ifPresent(next -> master.setNextStartDate(next.getNepaliDate()));
    master.setNewPensionStartDate(master.getNextStartDate());

    var statementBalance = statementReportRepository.getStatementBalanceAmtForPensioner(master.getPensionSubPNo());
    master.setStatementBalance(statementBalance);

    var groupedDetails = groupDetails(details, master.getPensionSubPNo());
    BigDecimal calculationAmount = new BigDecimal(0);
    for (var d : groupedDetails) {
      var amountToAdd = d.getTotalAmount();
      calculationAmount = calculationAmount.add(BigDecimal.valueOf(amountToAdd));
    }
    master.setCalculationAmount(calculationAmount.doubleValue());
    responseDto.setMaster(master);
    responseDto.setDetails(groupedDetails);
    return responseDto;
  }

  private List<VoucherTransactionDetail> groupDetails(List<VoucherTransactionDetail> details, String subPNo) {
    Map<Integer, List<VoucherTransactionDetail>> groups = new HashMap<>();
    for (var d : details) {
      groups.computeIfAbsent(d.getGroup(), k -> new ArrayList<>()).add(d);
    }

    var groupedDetails = new ArrayList<VoucherTransactionDetail>();
    for (var k : groups.keySet()) {
      var detail = generateGroupedDetail(groups.get(k));
      detail.setPensionSubPNo(subPNo);
      groupedDetails.add(detail);
    }

    return groupedDetails;
  }

  private VoucherTransactionDetail generateGroupedDetail(List<VoucherTransactionDetail> details) {
    var first = details.get(0);
    var last = details.get(details.size() - 1);
    var periods = details.stream().mapToInt(VoucherTransactionDetail::getPeriod).sum();

    var totalReceivable = new BigDecimal(0);
    var totalReceived = new BigDecimal(0);
    for (var d : details) {
      totalReceivable = totalReceivable.add(BigDecimal.valueOf(d.getReceivableAmount()));
      totalReceived = totalReceived.add(BigDecimal.valueOf(d.getReceiveAmount()));
    }

    var detail = new VoucherTransactionDetail();
    detail.setStartDate(first.getStartDate());
    detail.setEndDate(last.getEndDate());
    detail.setPeriod(periods);
    detail.setPeriodType(first.getPeriodType());
    detail.setReceivableAmount(first.getReceivableAmount());
    detail.setReceiveAmount(0);
    detail.setDifference(detail.getReceivableAmount() - detail.getReceiveAmount());
    detail.setTotalAmount(totalReceivable.subtract(totalReceived).doubleValue());
    detail.setIncrement(first.getIncrement());

    return detail;
  }

  /**
   * 
   * @param date the date to which we have to find the increment
   * @param increments list of increments
   * @param receivePercent It is used to calculate the final increment. Final increment is the percentage of this value
   * @return the actual incremnent, after receive percent adjustment
   */
  private double getIncrementAmount(NepaliCalendar date, List<PensionIncrement> increments, double incrementPercent) {
    double increment = 0;

    // this loop helps us find the increment for the provided date (if any)
    for (var i : increments) {
      var incrementDateParts = Arrays.stream(i.getIncrementDate().split("-")).toList();
      var incrementYear = Integer.parseInt(incrementDateParts.get(0));
      var incrementMonth = Integer.parseInt(incrementDateParts.get(1));
      var incrementDay = Integer.parseInt(incrementDateParts.get(2));

      // Increment date must be the first day of the month
      if (incrementDay != 1)
        throw new BadRequestException("Increment date doesn't start from first day of month");

      if (date.getNepaliYear() == incrementYear && date.getNepaliMonth() == incrementMonth
          && date.getNepaliDay() == incrementDay) {
        increment = i.getIncrementAmount();
        break; // we found the increment, lets stop the loop
      }
    }

    var df = new DecimalFormat("#.##");

    var actualIncrement = increment * incrementPercent / 100;
    return Double.parseDouble(df.format(actualIncrement));
  }

  private VoucherTransactionDetail generateDetail(String start, String end, int period, int type, double amount,
      double increment, int group) {
    DecimalFormat df = new DecimalFormat("#.##");

    var detail = new VoucherTransactionDetail();
    detail.setStartDate(start);
    detail.setEndDate(end);
    detail.setPeriod(period);
    detail.setPeriodType(type);

    detail.setReceivableAmount(Math.round(amount));
    detail.setReceiveAmount(0);
    detail.setDifference(detail.getReceivableAmount()); // because we have set ReceiveAmount as 0
    detail.setIncrement(Double.parseDouble(df.format(increment)));
    detail.setGroup(group);

    return detail;
  }

  private String formatDate(int year, int month, int day) {
    return String.format("%d-%02d-%02d", year, month, day);
  }
}
