package com.v1.pension.service.pensioner;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;

import jakarta.persistence.Tuple;

import org.springframework.stereotype.Service;

import com.v1.pension.exception.BadRequestException;
import com.v1.pension.exception.ResourceNotFoundException;
import com.v1.pension.model.pensioner.BankAccount;
import com.v1.pension.model.pensioner.PensionerPresence;
import com.v1.pension.model.pensioner.PensionerRegistration;
import com.v1.pension.repository.pensioner.BankAccountRepository;
import com.v1.pension.repository.pensioner.PensionPresenceRepository;
import com.v1.pension.repository.pensioner.PensionerRegistrationRepository;
import com.v1.pension.repository.system.NepaliCalendarRepository;
import com.v1.pension.utils.CurrentUser;

@Service
@RequiredArgsConstructor
public class BankAccountService {
  private final BankAccountRepository bankAccountRepository;
  private final PensionerRegistrationRepository pensionerRegistrationRepository;
  private final NepaliCalendarRepository nepaliCalendarRepository;
  private final PensionPresenceRepository presenceRepository;

  public List<Tuple> getAllBySubPNo(String subPNo) {
    return bankAccountRepository.getAllBySubPNo(subPNo);
  }

  public BankAccount getById(long id) {
    return bankAccountRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Bank Account Details Not Found"));
  }

  public void savePensionerBankAccount(BankAccount request) {
    var isNewAccount = request.getId() == null;
    var totalBankAccounts = bankAccountRepository.getBankAccountCountForPensioner(request.getPensionSubPNo());

    var pensioner = pensionerRegistrationRepository.findByPensionSubPNo(request.getPensionSubPNo())
        .orElseThrow(() -> new ResourceNotFoundException("Pensioner Not Found"));

    if (!pensioner.getPensionBankCode().equals(CurrentUser.getOfficeCode()))
      throw new BadRequestException("Only concerned bank can update pensioner's bank account");

    var extraActiveAcCount = bankAccountRepository
        .getExtraActiveBankAccountCountForPensioner(request.getPensionSubPNo(), request.getAccountNo());
    if (request.getStatus() == 1 && extraActiveAcCount > 0)
      throw new BadRequestException("Cannot have more than 1 active bank account");

    if (request.getStatus() == 1) {
      pensioner.setPensionAccountNo(request.getAccountNo());

      // update pensioner name
      var accountName = request.getAccountName().strip();
      request.setAccountName(accountName);
      if (pensioner.getPensionSubPensioner() == 0) {
        pensioner.setPensionNameEng(accountName);
      } else {
        pensioner.setPensionSubNameEng(accountName);
      }
    } else {
      pensioner.setPensionAccountNo(null);
    }

    pensionerRegistrationRepository.save(pensioner);
    bankAccountRepository.save(request);

    if (isNewAccount && totalBankAccounts == 0) {
      savePensionPresenceInfo(pensioner);
    }
  }

  private void savePensionPresenceInfo(PensionerRegistration pensioner) {
    var dateEntityOpt = nepaliCalendarRepository.findByRomanDate(LocalDate.now());
    if (dateEntityOpt.isEmpty())
      throw new BadRequestException("Date not found in database");

    var presenceData = new PensionerPresence();
    presenceData.setPensionSubPNo(pensioner.getPensionSubPNo());
    presenceData.setPresentDate(dateEntityOpt.get().getNepaliDate());
    presenceData.setPresentDateEng(dateEntityOpt.get().getRomanDate());
    presenceData.setNid(pensioner.getNationalId());
    presenceData.setFileData(null);
    presenceData.setType(3); // manual presence
    presenceRepository.save(presenceData);
  }

  public void closeBankAccountForPensioner(String subPNo, int status) {
    var pensioner = pensionerRegistrationRepository.findByPensionSubPNo(subPNo)
        .orElseThrow(() -> new ResourceNotFoundException("Pensioner Not Found"));

    if (!pensioner.getPensionBankCode().equals(CurrentUser.getOfficeCode()))
      throw new BadRequestException("Only concerned bank can update pensioner's bank account");

    pensioner.setPensionStatus(status);
    pensioner.setPensionAccountNo(null);
    pensionerRegistrationRepository.save(pensioner);

    bankAccountRepository.inactivateAccountsForPensioner(subPNo);
  }
}
