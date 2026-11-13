package com.v1.pension.service.pensioner;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;

import jakarta.persistence.Tuple;

import org.springframework.stereotype.Service;

import com.v1.pension.dto.pensioner.voucher_transaction.YearlyTaxableParametersDto;
import com.v1.pension.enums.MonthName;
import com.v1.pension.exception.BadRequestException;
import com.v1.pension.exception.ResourceNotFoundException;
import com.v1.pension.model.pensioner.VoucherTransactionDetail;
import com.v1.pension.model.pensioner.VoucherTransactionMaster;
import com.v1.pension.repository.other.IpfmsStatusUpdateRepository;
import com.v1.pension.repository.pensioner.VoucherTransactionDetailRepository;
import com.v1.pension.repository.pensioner.VoucherTransactionMasterRepository;
import com.v1.pension.service.other.IpfmsService;
import com.v1.pension.utils.CurrentUser;
import com.v1.pension.utils.DbUtils;

@Service
@RequiredArgsConstructor
public class PaymentVoucherService {
  private final VoucherTransactionMasterRepository masterRepository;
  private final VoucherTransactionDetailRepository detailRepository;
  private final IpfmsService ipfmsService;
  private final IpfmsStatusUpdateRepository statusUpdateRepository;

  public List<Tuple> getPaymentVoucherTransactions(int status) {
    return masterRepository.getPaymentVoucherTransactions(CurrentUser.getFiscalYear(), status);
  }

  public void generateManachamalPayment(VoucherTransactionMaster master) {
    masterRepository.updatePensionerBalanceView();

    master.setVoucherNo(masterRepository.getNextVoucherNumber(CurrentUser.getFiscalYear()));
    master.setFiscalYear(CurrentUser.getFiscalYear());
    master.setOfficeCode(CurrentUser.getOfficeCode());
    master.setStage(DbUtils.getReceiveStageCodeForRole(CurrentUser.getUserRole()));
    master.setVoucherType(4);
    master.setPaymentType(3); // Mana-Chamal

    if (masterRepository.existsPendingPayment(master.getPaymentType(), master.getPensionerTypeCode(),
        0))
      throw new BadRequestException("There is pending payment to be approved for this type");

    masterRepository.save(master);

    var types = master.getPensionerTypeCode().equals("1") ? List.of("1", "2", "4")
        : List.of(master.getPensionerTypeCode());
    var pensioners = detailRepository.getManachamalPaymentInfo(types);

    List<VoucherTransactionDetail> details = new ArrayList<>();

    for (var p : pensioners) {
      var subPNo = p.getPensionNumber();

      var d = new VoucherTransactionDetail();
      d.setPensionSubPNo(subPNo);
      d.setMasterId(master.getId());
      d.setFiscalYear(CurrentUser.getFiscalYear());
      d.setReceivableAmount(p.getBalance() * -1);
      d.setTax(0);
      d.setTotalAmount(d.getReceivableAmount() - d.getTax());

      details.add(d);
    }

    detailRepository.saveAll(details);
  }

  public void generateTameliPayment(VoucherTransactionMaster master) {
    masterRepository.updatePensionerBalanceView();

    master.setVoucherNo(masterRepository.getNextVoucherNumber(CurrentUser.getFiscalYear()));
    master.setFiscalYear(CurrentUser.getFiscalYear());
    master.setOfficeCode(CurrentUser.getOfficeCode());
    master.setStage(DbUtils.getReceiveStageCodeForRole(CurrentUser.getUserRole()));
    master.setVoucherType(4);
    master.setPaymentType(2); // Tameli

    if (masterRepository.existsPendingPayment(master.getPaymentType(), master.getPensionerTypeCode(),
        0))
      throw new BadRequestException("There is pending payment to be approved for this type");

    masterRepository.save(master);

    var types = master.getPensionerTypeCode().equals("1") ? List.of("1", "2", "4")
        : List.of(master.getPensionerTypeCode());
    var pensioners = detailRepository.getTameliPaymentInfo(types, CurrentUser.getFiscalYear());

    List<VoucherTransactionDetail> details = new ArrayList<>();

    for (var p : pensioners) {
      var subPNo = p.getPensionNumber();
      var gender = p.getPensionGender();

      var d = new VoucherTransactionDetail();
      d.setPensionSubPNo(subPNo);
      d.setMasterId(master.getId());
      d.setFiscalYear(CurrentUser.getFiscalYear());
      d.setReceivableAmount(p.getBalance() * -1);

      if (p.getLegalCase()) {
        d.setTax(0);
      } else {
        var params = new YearlyTaxableParametersDto(
            subPNo,
            p.getOtherTypeCode(),
            p.getPensionMaritalStatus(),
            3,
            p.getBalance(),
            0D,
            3,
            -1L);

        double yearlyTaxableAmount = getYearlyTaxableAmount(params);
        var taxAmt = calculateTaxAmount(subPNo, gender, yearlyTaxableAmount);
        d.setTax(taxAmt * -1);
      }

      d.setTotalAmount(d.getReceivableAmount() - d.getTax());

      details.add(d);
    }

    detailRepository.saveAll(details);
  }

  public void generateRegularPayment(VoucherTransactionMaster master) {
    masterRepository.updatePensionerBalanceView();

    master.setVoucherNo(masterRepository.getNextVoucherNumber(CurrentUser.getFiscalYear()));
    master.setFiscalYear(CurrentUser.getFiscalYear());
    master.setOfficeCode(CurrentUser.getOfficeCode());
    master.setStage(DbUtils.getReceiveStageCodeForRole(CurrentUser.getUserRole()));
    master.setVoucherType(4);
    master.setPaymentType(1);

    if (masterRepository.existsPendingPayment(master.getPaymentType(), master.getPensionerTypeCode(),
        master.getMainBankCode()))
      throw new BadRequestException("There is pending payment to be approved for this type");

    masterRepository.save(master);

    var pensioners = detailRepository.getRegularPayablePensionersInfo(master.getMainBankCode(),
        master.getStatementDate(), master.getPensionerTypeCode(), CurrentUser.getFiscalYear());

    List<VoucherTransactionDetail> details = new ArrayList<>();

    for (var p : pensioners) {
      var subPNo = p.getSubNumber();
      var gender = p.getPensionGender();
      var lastMonthlyGeneratedMonth = Integer.parseInt(p.getLastMonthlyPensionDate().split("-")[1]);

      var d = new VoucherTransactionDetail();
      d.setPensionSubPNo(subPNo);
      d.setMasterId(master.getId());
      d.setFiscalYear(CurrentUser.getFiscalYear());
      d.setReceivableAmount(p.getBalance() * -1);

      if (p.getLegalCase()) {
        d.setTax(0);
      } else {
        var params = new YearlyTaxableParametersDto(
            subPNo,
            p.getOtherTypeCode(),
            p.getPensionMaritalStatus(),
            p.getFestivalMonth(),
            p.getBalance(),
            p.getMonthlyAmount(),
            lastMonthlyGeneratedMonth,
            -1L);

        double yearlyTaxableAmount = getYearlyTaxableAmount(params);
        var taxAmt = calculateTaxAmount(subPNo, gender, yearlyTaxableAmount);
        d.setTax(taxAmt * -1);
      }

      d.setTotalAmount(d.getReceivableAmount() - d.getTax());

      details.add(d);
    }

    detailRepository.saveAll(details);
  }

  public void generateDailyPayment(VoucherTransactionMaster master) {
    masterRepository.updatePensionerBalanceView();

    master.setVoucherNo(masterRepository.getNextVoucherNumber(CurrentUser.getFiscalYear()));
    master.setFiscalYear(CurrentUser.getFiscalYear());
    master.setOfficeCode(CurrentUser.getOfficeCode());
    master.setStage(DbUtils.getReceiveStageCodeForRole(CurrentUser.getUserRole()));
    master.setVoucherType(4);
    master.setPaymentType(4); // 4 = Daily payment

    if (masterRepository.existsPendingPayment(master.getPaymentType(), master.getPensionerTypeCode(),
        0))
      throw new BadRequestException("There is pending payment to be approved for this type");

    masterRepository.save(master);

    var types = master.getPensionerTypeCode().equals("1") ? List.of("1", "2", "4")
        : List.of(master.getPensionerTypeCode());
    var pensioners = detailRepository.getDailyPayablePensionersInfo(master.getStatementDate(), types,
        CurrentUser.getFiscalYear());

    List<VoucherTransactionDetail> details = new ArrayList<>();

    for (var p : pensioners) {
      var subPNo = p.getSubNumber();
      var gender = p.getPensionGender();
      var lastMonthlyGeneratedMonth = Integer.parseInt(p.getLastMonthlyPensionDate().split("-")[1]);

      var d = new VoucherTransactionDetail();
      d.setPensionSubPNo(subPNo);
      d.setMasterId(master.getId());
      d.setFiscalYear(CurrentUser.getFiscalYear());
      d.setReceivableAmount(p.getBalance() * -1);

      if (p.getLegalCase()) {
        d.setTax(0);
      } else {
        var params = new YearlyTaxableParametersDto(
            subPNo,
            p.getOtherTypeCode(),
            p.getPensionMaritalStatus(),
            p.getFestivalMonth(),
            p.getBalance(),
            p.getMonthlyAmount(),
            lastMonthlyGeneratedMonth,
            -1L);

        double yearlyTaxableAmount = getYearlyTaxableAmount(params);
        var taxAmt = calculateTaxAmount(subPNo, gender, yearlyTaxableAmount);
        d.setTax(taxAmt * -1);
      }

      d.setTotalAmount(d.getReceivableAmount() - d.getTax());

      details.add(d);
    }

    detailRepository.saveAll(details);
  }

  private double calculateTaxAmount(String subPNo, String gender, double yearlyTaxableAmount) {
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

    if (gender.equals("2"))
      tax = tax - 0.1 * tax; // 10% discount on tax for female

    var totalTaxPaidForThisFy = detailRepository.getTotalPaidTaxForFy(subPNo, CurrentUser.getFiscalYear());

    // deduct total paid tax in this fy. so that we don't deduct tax each month
    tax = tax - totalTaxPaidForThisFy;

    if (tax < 0)
      return 0;

    DecimalFormat df = new DecimalFormat("#.##");
    return Double.parseDouble(df.format(tax));
  }

  private double getYearlyTaxableAmount(YearlyTaxableParametersDto dto) {
    var monthName = MonthName.getByCalendarMonth(dto.month());
    Double balanceAmount = dto.receivedAmount();

    boolean isDisable = dto.otherTypeCode().equals("3");
    double taxAdvantage = 10_00_000; // 10 lakh
    taxAdvantage = taxAdvantage + taxAdvantage * 0.25; // pensioner advantage
    if (isDisable)
      taxAdvantage = taxAdvantage + taxAdvantage * 0.5; // disable advantage

    var festiveMonthName = MonthName.getByCalendarMonth(dto.festivalMonth());
    var incrementMonth = monthName.getFyMonth() <= festiveMonthName.getFyMonth() ? 1 : 0;
    double yearlyAmount = dto.monthlyAmount() * (12 - monthName.getFyMonth() + incrementMonth) + balanceAmount;

    return yearlyAmount - taxAdvantage;
  }

  public void updatePaymentStatus(long masterId) {
    var vm = masterRepository.findById(masterId)
        .orElseThrow(() -> new ResourceNotFoundException("Voucher Transaction Not Found"));

    var statusCheckResponse = ipfmsService.checkPaymentStatus(vm).block();

    if (statusCheckResponse == null)
      throw new BadRequestException("Failed to check status");

    if (!statusCheckResponse.isSuccess())
      throw new BadRequestException(statusCheckResponse.getMessage());

    var resultId = statusUpdateRepository.saveResult(masterId, statusCheckResponse.getJsonData());

    statusUpdateRepository.updatePaymentStatus(resultId, masterId);

    vm.setStatus(3);
    masterRepository.save(vm);

    detailRepository.deleteEftIneligibleDetails(masterId);

    statusUpdateRepository.deleteResult(resultId);
  }

  public void updateEftRejection(String eftNumbersSeparatedByComma, String remarks) {
    var eftNumbers = Stream.of(eftNumbersSeparatedByComma.split(","))
        .filter(Objects::nonNull)
        .map(x -> x.strip()).toList();
    var transactionDetails = detailRepository.getTransactionDetailsByEftNumbers(eftNumbers);

    for (var d : transactionDetails) {
      d.setStatus(4); // 4 = Rejected
      d.setEftRemarks(remarks);
    }

    detailRepository.saveAll(transactionDetails);
  }
}
