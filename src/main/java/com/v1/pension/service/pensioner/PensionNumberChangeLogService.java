package com.v1.pension.service.pensioner;

import java.util.List;
import lombok.RequiredArgsConstructor;

import jakarta.persistence.Tuple;

import org.springframework.stereotype.Service;

import com.v1.pension.dto.other.PensionNumberChangeRequestDto;
import com.v1.pension.exception.BadRequestException;
import com.v1.pension.model.pensioner.PensionNumberChangeLog;
import com.v1.pension.repository.pensioner.PensionNumberChangeLogRepository;
import com.v1.pension.repository.pensioner.PensionerRegistrationRepository;

@Service
@RequiredArgsConstructor
public class PensionNumberChangeLogService {
  private final PensionNumberChangeLogRepository pensionNumberChangeLogRepository;
  private final PensionerRegistrationRepository pensionerRegistrationRepository;

  public List<Tuple> getPensionNumberChangeLogs() {
    return pensionNumberChangeLogRepository.getPensionNumberChangeLogs();
  }

  public void changePensionNumber(PensionNumberChangeRequestDto dto) {
    if (!pensionerRegistrationRepository.existsByPensionSubPNo(dto.getOldValue()))
      throw new BadRequestException("Pensioner not found");

    pensionNumberChangeLogRepository.changePensionNumber(dto.getOldValue(), dto.getNewValue());

    var entity = new PensionNumberChangeLog();
    entity.setOldValue(dto.getOldValue());
    entity.setNewValue(dto.getNewValue());
    pensionNumberChangeLogRepository.save(entity);
  }
}
