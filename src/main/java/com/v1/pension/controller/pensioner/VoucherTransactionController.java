package com.v1.pension.controller.pensioner;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.v1.pension.dto.pensioner.voucher_transaction.VoucherCalculationDto;
import com.v1.pension.dto.pensioner.voucher_transaction.VoucherTaxCalculationDto;
import com.v1.pension.exception.BadRequestException;
import com.v1.pension.exception.ResourceNotFoundException;
import com.v1.pension.model.pensioner.ForwardedDataDetails;
import com.v1.pension.model.pensioner.VoucherTransactionMaster;
import com.v1.pension.repository.pensioner.ForwardedDataRepository;
import com.v1.pension.repository.pensioner.VoucherTransactionDetailRepository;
import com.v1.pension.repository.pensioner.VoucherTransactionMasterRepository;
import com.v1.pension.service.other.IpfmsService;
import com.v1.pension.service.pensioner.AdjustmentVoucherService;
import com.v1.pension.service.pensioner.MonthlyVoucherService;
import com.v1.pension.service.pensioner.PaymentVoucherUpdatedService;
import com.v1.pension.utils.CurrentUser;
import com.v1.pension.utils.DbUtils;
import com.v1.pension.utils.MyResponse;

@RestController
@RequestMapping("/api/voucher-transactions")
@RequiredArgsConstructor
public class VoucherTransactionController {
  private final AdjustmentVoucherService adjustmentVoucherService;
  private final MonthlyVoucherService monthlyVoucherService;
  // private final PaymentVoucherService paymentVoucherService;
  private final PaymentVoucherUpdatedService paymentVoucherService;
  private final VoucherTransactionMasterRepository voucherTransactionMasterRepository;
  private final VoucherTransactionDetailRepository voucherTransactionDetailRepository;
  private final ForwardedDataRepository forwardedDataRepository;
  private final ModelMapper modelMapper;
  private final IpfmsService ipfmsService;

  private static final String VOUCHER_NOT_FOUND_STRING = "Voucher Transaction Not Found";

  @PostMapping("/voucher-tax-calculation")
  public ResponseEntity<?> voucherTaxCalculation(@RequestBody VoucherTaxCalculationDto dto) {
    var taxAmt = adjustmentVoucherService.generateTaxForAdjustment(dto);
    var df = new DecimalFormat("#");
    var tax = Double.parseDouble(df.format(taxAmt));
    return ResponseEntity.ok(Map.of("taxAmount", tax));
  }

  @GetMapping("/approval-pending-vouchers")
  public ResponseEntity<?> getApprovalPendingVouchers() {
    var roleName = CurrentUser.getUserRole();
    var data = voucherTransactionMasterRepository.getApprovalPendingTransactions(roleName, CurrentUser.getOfficeCode());
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getVoucherInfoById(@PathVariable long id) {
    var data = adjustmentVoucherService.getVoucherInfoById(id);
    return ResponseEntity.ok(data);
  }

  @PostMapping("/{id}/backward-to-dtco")
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public ResponseEntity<?> backwardToDtco(@PathVariable long id, @RequestBody Map<String, String> dto) {
    var note = dto.get("note");
    var master = voucherTransactionMasterRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(VOUCHER_NOT_FOUND_STRING));
    var forwardDetails = forwardedDataRepository.findById(master.getForwardId())
        .orElseThrow(() -> new ResourceNotFoundException("Forward Info Not Found"));

    var newForwardDetails = new ForwardedDataDetails();
    modelMapper.map(forwardDetails, newForwardDetails);
    newForwardDetails.setId(null);
    newForwardDetails.setAction(0);
    newForwardDetails.setActionDate("");
    newForwardDetails.setSourceUserId(CurrentUser.getUserId());
    newForwardDetails.setSourceStage(DbUtils.getReceiveStageCodeForRole(CurrentUser.getUserRole()));
    newForwardDetails.setNote(note);
    forwardedDataRepository.save(newForwardDetails);

    master.setForwardId(newForwardDetails.getId());
    master.setStage(11);
    master.setNote(newForwardDetails.getNote());
    voucherTransactionMasterRepository.save(master);

    return ResponseEntity.ok(MyResponse.message("Backward Success"));
  }

  @GetMapping("/{id}/delete")
  public ResponseEntity<?> deleteVoucherTransaction(@PathVariable long id) {
    var voucher = voucherTransactionMasterRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(VOUCHER_NOT_FOUND_STRING));
    var details = voucherTransactionDetailRepository.getDetailsByMaster(id);

    if (List.of(4, 8, 12).contains(voucher.getStage()))
      throw new BadRequestException("Cannot Delete Approved Transaction");

    voucherTransactionDetailRepository.deleteAll(details);
    voucherTransactionMasterRepository.delete(voucher);
    return ResponseEntity.ok(MyResponse.message("Voucher transaction has been deleted"));
  }

  @PostMapping("/{id}/void")
  @PreAuthorize("hasAnyAuthority('PMO_APPROVER', 'ROOT')")
  public ResponseEntity<?> voidVoucherTransaction(@PathVariable long id, @RequestBody Map<String, String> dto) {
    var voucher = voucherTransactionMasterRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(VOUCHER_NOT_FOUND_STRING));
    if (!CurrentUser.getUserRole().equals("ROOT") && voucher.getStage() != 4)
      throw new BadRequestException("Non-Approved Voucher Cannot Be Void");

    long maxIdFromMigration = 142820925;
    var lastPaymentId = voucherTransactionMasterRepository
        .getLastPaymentVoucherIdForPensioner(voucher.getPensionSubPNo());
    if (id > maxIdFromMigration && id < lastPaymentId)
      throw new BadRequestException("Failed: payment has alreay been made");

    var note = dto.get("note");
    if (!StringUtils.hasText(note))
      throw new BadRequestException("Please specify note for void");

    if (!CurrentUser.getUserRole().equals("ROOT")) {
      var fd = forwardedDataRepository.findById(voucher.getForwardId())
          .orElseThrow(() -> new ResourceNotFoundException("Forward info not found"));

      if (fd.getReceiveUserId() != null && !fd.getReceiveUserId().equals(CurrentUser.getUserId()))
        throw new BadRequestException("This voucher has not been approved by you");
    }

    voucher.setStage(100);
    voucher.setNote(note);
    voucherTransactionMasterRepository.save(voucher);
    return ResponseEntity.ok(MyResponse.message("Voucher Transaction Has Been Void"));
  }

  // ------- Adjustments -------------
  @GetMapping("/adjustments/latest-id")
  public ResponseEntity<Object> getLatestAdjustmentVoucherIdForPensioner(@RequestParam String subPNo) {
    var voucherIdOpt = voucherTransactionMasterRepository
        .getLatestAdjustmentVoucherIdForPensioner(subPNo);
    if (voucherIdOpt.isEmpty()) {
      throw new BadRequestException("Adjustment Voucher Not Found");
    }

    return ResponseEntity.ok(Map.of("id", voucherIdOpt.get()));
  }

  @GetMapping("/adjustments")
  public ResponseEntity<?> getAdjustmentTransactionsForPensioner(@RequestParam String subPNo) {
    var data = adjustmentVoucherService.getAdjustmentTransactionsForPensioner(subPNo);
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @PostMapping("/adjustments/save")
  public ResponseEntity<?> saveAdjustmentVoucherTransaction(@RequestBody VoucherCalculationDto dto) {
    var lastAdjustmentVoucherId = voucherTransactionMasterRepository
        .getLastAdjustmentVoucherIdForPensioner(dto.getMaster().getPensionSubPNo());
    var lastAdjustmentVoucherAmount = voucherTransactionMasterRepository
        .getLastAdjustmentVoucherAmountForId(lastAdjustmentVoucherId);
    var lastPaymentVoucherId = voucherTransactionMasterRepository
        .getLastPaymentVoucherIdForPensioner(dto.getMaster().getPensionSubPNo());
    long maxIdFromMigration = 142820925;

    if (dto.getMaster().getId() == null && lastAdjustmentVoucherId > maxIdFromMigration
        && lastAdjustmentVoucherId > lastPaymentVoucherId && lastAdjustmentVoucherId > 0
        && lastAdjustmentVoucherAmount > 0)
      throw new BadRequestException("यस निवृत्तभरकको पुरानो भौचरको भुक्तानी हुन बाँकी रहेको ।");

    var masterId = adjustmentVoucherService.saveAdjustmentVoucherTransaction(dto.getMaster(), dto.getDetails());
    return ResponseEntity.ok(MyResponse.message("Transaction Has Been Saved", masterId));
  }

  @PostMapping("/adjustments/calculation")
  public ResponseEntity<?> calculateAdjustment(@RequestBody VoucherTransactionMaster master) {
    var details = adjustmentVoucherService.generateVoucherCalculation(master);
    return ResponseEntity.ok(details);
  }

  // --------- Monthly Voucher ------------
  @GetMapping("/monthly/all")
  public ResponseEntity<?> getMonthlyVoucherTransactions() {
    var data = monthlyVoucherService.getMasterList();
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/monthly/{masterId}")
  public ResponseEntity<?> getMonthlyVoucherById(@PathVariable long masterId) {
    var data = monthlyVoucherService.getMasterById(masterId);
    return ResponseEntity.ok(data);
  }

  @PostMapping("/monthly")
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public ResponseEntity<Object> generateMonthlyVoucher(@RequestBody VoucherTransactionMaster master) {
    monthlyVoucherService.generateMonthlyPension(master);
    return ResponseEntity.ok(MyResponse.message("Monthly Pension Has Been Generated"));
  }

  // Payment Voucher
  @GetMapping("/payments/all")
  public ResponseEntity<?> getPaymentVoucherTransactions(@RequestParam(defaultValue = "-1") int status) {
    var data = paymentVoucherService.getPaymentVoucherTransactions(status);
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @PostMapping("/regular-payments")
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public ResponseEntity<Object> generateRegularPayments(@RequestBody VoucherTransactionMaster master) {
    paymentVoucherService.generateRegularPayment(master);
    return ResponseEntity.ok(MyResponse.message("Payment has been generated"));
  }

  @PostMapping("/tameli-payments")
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public ResponseEntity<Object> generateTameliPayments(@RequestBody VoucherTransactionMaster master) {
    paymentVoucherService.generateTameliPayment(master);
    return ResponseEntity.ok(MyResponse.message("Payment has been generated"));
  }

  @PostMapping("/manachamal-payments")
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public ResponseEntity<Object> generateManachamalPayments(@RequestBody VoucherTransactionMaster master) {
    paymentVoucherService.generateManachamalPayment(master);
    return ResponseEntity.ok(MyResponse.message("Payment has been generated"));
  }

  @PostMapping("/{id}/send-to-ipfms")
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public ResponseEntity<Object> sendToIpfms(@PathVariable long id, @RequestBody Map<String, String> data) {
    var date = data.get("date");
    ipfmsService.sendBulkPaymentToIpfms(id, date);
    return ResponseEntity.ok(MyResponse.message("Successfully sent to ipfms"));
  }

  @GetMapping("/{id}/json-data")
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public ResponseEntity<Object> getIpfmsPaymentData(@PathVariable long id) {
    var data = ipfmsService.getIpfmsPaymentData(id);
    return ResponseEntity.ok(data);
  }

  @GetMapping("/{id}/ipfms-status-check")
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public ResponseEntity<Object> updatePaymentStatus(@PathVariable long id) {
    paymentVoucherService.updatePaymentStatus(id);
    return ResponseEntity.ok(MyResponse.message("Payment Status Has Been Updated"));
  }

  @PostMapping("/update-eft-rejections")
  @PreAuthorize("hasAuthority('ROOT')")
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public ResponseEntity<Object> updateEftRejections(@RequestBody Map<String, String> dto) {
    var eftNumbersSeparatedByComma = dto.get("eftNos");
    var remarks = dto.get("remarks");

    if (!StringUtils.hasText(eftNumbersSeparatedByComma))
      throw new BadRequestException("Please specify eft numbers separated by comma");
    if (!StringUtils.hasText(remarks))
      throw new BadRequestException("Please specify remarks for eft rejection");

    paymentVoucherService.updateEftRejection(eftNumbersSeparatedByComma, remarks);
    return ResponseEntity.ok(MyResponse.message("Update Successful"));
  }
}
