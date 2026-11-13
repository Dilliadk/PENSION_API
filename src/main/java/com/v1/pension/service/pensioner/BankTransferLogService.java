package com.v1.pension.service.pensioner;

import java.util.List;
import lombok.RequiredArgsConstructor;

import jakarta.persistence.Tuple;

import org.springframework.stereotype.Service;

import com.v1.pension.exception.ResourceNotFoundException;
import com.v1.pension.model.pensioner.BankTransferLog;
import com.v1.pension.repository.pensioner.BankTransferLogRepository;
import com.v1.pension.repository.pensioner.PensionerRegistrationRepository;

@Service
@RequiredArgsConstructor
public class BankTransferLogService {
  private final BankTransferLogRepository bankTransferLogRepository;
  private final PensionerRegistrationRepository pensionerRegistrationRepository;

  public List<Tuple> getPensionerBankTransferLogs(String subPNo) {
    return bankTransferLogRepository.getBankTransferLogsForPensioner(subPNo);
  }

  public void transferBank(BankTransferLog request) {
    var pensioner = pensionerRegistrationRepository.findByPensionSubPNo(request.getPensionSubPNo())
        .orElseThrow(() -> new ResourceNotFoundException("Pensioner Not Found"));
    request.setOldMainBankCode(pensioner.getMainBankCode());
    request.setOldPensionBankCode(pensioner.getPensionBankCode());

    bankTransferLogRepository.save(request);

    pensioner.setMainBankCode(request.getNewMainBankCode());
    pensioner.setPensionBankCode(request.getNewPensionBankCode());

    // since bank has been changed, we need to update account no. as well
    pensioner.setPensionAccountNo(null);

    pensionerRegistrationRepository.save(pensioner);
  }
}
