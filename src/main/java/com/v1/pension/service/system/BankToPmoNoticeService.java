package com.v1.pension.service.system;

import com.v1.pension.exception.BadRequestException;
import com.v1.pension.exception.ResourceNotFoundException;
import com.v1.pension.model.system.BankToPmoNotice;
import com.v1.pension.repository.pensioner.PensionerRegistrationRepository;
import com.v1.pension.repository.system.BankToPmoNoticeRepository;
import com.v1.pension.utils.CurrentUser;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BankToPmoNoticeService {
  private final BankToPmoNoticeRepository bankToPmoNoticeRepository;
  private final PensionerRegistrationRepository pensionerRegistrationRepository;

  public List<Tuple> getActiveBankToPmoNoticeList() {
    return bankToPmoNoticeRepository.getActiveNotices();
  }

  public void saveBankToPmoNotice(BankToPmoNotice request) {
    var pensioner = pensionerRegistrationRepository.findByPensionSubPNo(request.getPensionSubPNo())
        .orElseThrow(() -> new ResourceNotFoundException("Pensioner With This Pension No. Not Found"));

    if (!pensioner.getPensionBankCode().equals(CurrentUser.getOfficeCode()))
      throw new BadRequestException("This pensioner does not belong to this bank");

    request.setBankBranchCode(pensioner.getPensionBankCode());
    bankToPmoNoticeRepository.save(request);
  }
}
