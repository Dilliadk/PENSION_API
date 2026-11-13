package com.v1.pension.service.pensioner;

import java.util.List;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import com.v1.pension.dto.forward_data.ApproveDataDto;
import com.v1.pension.dto.forward_data.BackwardDataDto;
import com.v1.pension.dto.forward_data.ForwardDataDto;
import com.v1.pension.enums.FormType;
import com.v1.pension.exception.BadRequestException;
import com.v1.pension.exception.ResourceNotFoundException;
import com.v1.pension.model.pensioner.BankAccountCloseInformation;
import com.v1.pension.model.pensioner.ForwardedDataDetails;
import com.v1.pension.repository.pensioner.BankAccountCloseInformationRepository;
import com.v1.pension.repository.pensioner.ForwardedDataRepository;
import com.v1.pension.repository.pensioner.PensionerRegistrationRepository;
import com.v1.pension.repository.pensioner.VoucherTransactionMasterRepository;
import com.v1.pension.utils.CurrentUser;

@Service
@RequiredArgsConstructor
public class ForwardedDataService {
  private final ForwardedDataRepository forwardedDataRepository;
  private final VoucherTransactionMasterRepository voucherTransactionMasterRepository;
  private final PensionerRegistrationRepository pensionerRegistrationRepository;
  private final BankAccountCloseInformationRepository bankAccountCloseInformationRepository;

  public void saveForwardData(ForwardDataDto dto) {
    var userId = CurrentUser.getUserId();

    var vm = voucherTransactionMasterRepository.findById(dto.getMasterId())
        .orElseThrow(() -> new ResourceNotFoundException("Voucher Transacton not found"));
    if (vm.getStage() == dto.getReceiveStage())
      throw new BadRequestException("This voucher has already been forwarded");

    // creating new entity
    var entity = new ForwardedDataDetails();
    entity.setFormTypeId(dto.getFormTypeId());
    entity.setMasterId(dto.getMasterId());
    entity.setSourceStage(dto.getSourceStage());
    entity.setReceiveStage(dto.getReceiveStage());
    // entity.setReceiveUserId(dto.getReceiveUserId());
    entity.setNote(dto.getNote());
    entity.setSourceUserId(userId);
    entity.setAction(0);
    forwardedDataRepository.save(entity);

    updateMaster(entity.getFormTypeId(), entity.getMasterId(), entity.getReceiveStage(), entity.getId(), dto.getNote());
  }

  public void saveBackwardData(BackwardDataDto dto) {
    var userId = CurrentUser.getUserId();
    var latestData = forwardedDataRepository.findById(dto.getForwardId())
        .orElseThrow(() -> new ResourceNotFoundException("Forward Data Not Found"));
    var vm = voucherTransactionMasterRepository.findById(latestData.getMasterId())
        .orElseThrow(() -> new ResourceNotFoundException("Voucher Transacton not found"));

    if (vm.getStage() == dto.getReceiveStage())
      throw new BadRequestException("This voucher has already been backwarded");

    latestData.setAction(2); // 2 = backward
    latestData.setActionDate(dto.getActionDate());
    latestData.setReceiveUserId(CurrentUser.getUserId());

    // creating new backward entity
    var entity = new ForwardedDataDetails();
    entity.setFormTypeId(latestData.getFormTypeId());
    entity.setMasterId(latestData.getMasterId());
    entity.setSourceStage(dto.getSourceStage());
    if (CurrentUser.getUserRole().equals("PMO_APPROVER") && !vm.getOfficeCode().equals("00001")) {
      entity.setReceiveStage(12);
    } else {
      entity.setReceiveStage(dto.getReceiveStage());
    }
    entity.setSourceUserId(userId);
    entity.setNote(dto.getNote());

    forwardedDataRepository.save(latestData);
    forwardedDataRepository.save(entity);

    // var initialUserIds = getInitialUserIds(entity.getFormTypeId(), entity.getMasterId());
    // String userIds = "";
    // if (latestData.getReceiveStage() < latestData.getSourceStage()) {
    //   // backward followed by backward scenario
    //   // preserve previous sender
    //   userIds = DbUtils.getUserIds(initialUserIds, entity.getSourceUserId(), entity.getReceiveUserId(), null,
    //       latestData.getReceiveUserId());
    // } else {
    //   userIds = DbUtils.getUserIds(initialUserIds, entity.getSourceUserId(), entity.getReceiveUserId(),
    //       latestData.getSourceUserId(), latestData.getReceiveUserId());
    // }

    updateMaster(latestData.getFormTypeId(), entity.getMasterId(), entity.getReceiveStage(), entity.getId(),
        dto.getNote());
  }

  public void approveForm(ApproveDataDto dto) {
    var latestData = forwardedDataRepository.findById(dto.getForwardId())
        .orElseThrow(() -> new ResourceNotFoundException("Forward Data Not Found"));
    var master = voucherTransactionMasterRepository.findById(latestData.getMasterId())
        .orElseThrow(() -> new ResourceNotFoundException("Voucher Not Found"));

    var dtcoApproved = CurrentUser.getUserRole().equals("DTCO_APPROVER") && master.getStage() == 12;
    var pmoApproved = CurrentUser.getUserRole().equals("PMO_APPROVER") && master.getStage() == 4;
    if (latestData.getAction() == 1 || dtcoApproved || pmoApproved)
      throw new BadRequestException("Data has already been approved");

    latestData.setAction(1); // 1 = approve
    latestData.setActionDate(dto.getActionDate());
    latestData.setReceiveUserId(CurrentUser.getUserId());
    forwardedDataRepository.save(latestData);

    // tameli update
    if (master.getVoucherType() == 1 && master.getNewSchemeCode().equals("29")) {
      var pensioner = pensionerRegistrationRepository.findByPensionSubPNo(master.getPensionSubPNo())
          .orElseThrow(() -> new ResourceNotFoundException("Pensioner Not Found"));
      pensioner.setPensionStatus(4);
      pensionerRegistrationRepository.save(pensioner);
    }

    var approveStageCode = switch (CurrentUser.getUserRole()) {
      case "PMO_APPROVER" -> 4;
      case "BANK_APPROVER" -> 8;
      case "DTCO_APPROVER" -> 12;
      default -> 0;
    };

    var note = approveStageCode == 12 ? "Sent to PMO from DTCO" : "Data Has Been Approved";

    updateMaster(latestData.getFormTypeId(), latestData.getMasterId(), approveStageCode, dto.getForwardId(), note);

    createBankAccountCloseInformation(latestData.getMasterId());
  }

  private void createBankAccountCloseInformation(long id) {
    if (!CurrentUser.getUserRole().equals("PMO_APPROVER"))
      return;

    var vm = voucherTransactionMasterRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Voucher Not Found"));

    if (vm.getVoucherType() == 1 && List.of("4", "8", "9").contains(vm.getCalculationTypeCode())) {
      var pensioner = pensionerRegistrationRepository.findByPensionSubPNo(vm.getPensionSubPNo())
          .orElseThrow(() -> new ResourceNotFoundException("Pensioner Info Not Found"));

      var entity = new BankAccountCloseInformation();
      entity.setPensionSubPNo(vm.getPensionSubPNo());
      entity.setVoucherMaster(vm);
      entity.setFiscalYear(vm.getFiscalYear());
      entity.setOfficeCode(pensioner.getPensionBankCode());
      bankAccountCloseInformationRepository.save(entity);
    }
  }

  private void updateMaster(int formTypeId, long masterId, int stage, long forwardId, String note) {
    var formType = FormType.fromId(formTypeId);
    switch (formType) {
      case VOUCHER_TRANSACTION:
        voucherTransactionMasterRepository.updateForward(masterId, stage, forwardId, note);
        break;

      default:
        break;
    }
  }
}
