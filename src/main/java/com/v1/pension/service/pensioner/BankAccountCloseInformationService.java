package com.v1.pension.service.pensioner;

import java.util.List;
import lombok.RequiredArgsConstructor;

import jakarta.persistence.Tuple;

import org.springframework.stereotype.Service;

import com.v1.pension.dto.pensioner.bank_account_close_information.BankAccountCloseInformationUpdateDto;
import com.v1.pension.exception.ResourceNotFoundException;
import com.v1.pension.repository.pensioner.BankAccountCloseInformationRepository;
import com.v1.pension.repository.pensioner.VoucherTransactionMasterRepository;
import com.v1.pension.utils.CurrentUser;

@Service
@RequiredArgsConstructor
public class BankAccountCloseInformationService {
  private final BankAccountCloseInformationRepository bankAccountCloseInformationRepository;
  private final BankAccountService bankAccountService;
  private final VoucherTransactionMasterRepository voucherTransactionMasterRepository;

  public List<Tuple> getInformation() {
    return bankAccountCloseInformationRepository.getInformation(CurrentUser.getFiscalYear(),
        CurrentUser.getOfficeCode());
  }

  public void updateAccountCloseInformation(long id, BankAccountCloseInformationUpdateDto dto) {
    var entity = bankAccountCloseInformationRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Information Not Found"));
    var vm = entity.getVoucherMaster();

    entity.setDate(dto.getDate());
    entity.setAmount(dto.getAmount());
    entity.setStatus(2); // 2 = completed

    bankAccountCloseInformationRepository.save(entity);

    if (vm.getVoucherType() == 1 && List.of("8", "9").contains(vm.getCalculationTypeCode()))
      bankAccountService.closeBankAccountForPensioner(entity.getPensionSubPNo(), 4);
  }
}
