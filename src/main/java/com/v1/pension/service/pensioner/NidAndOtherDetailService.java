package com.v1.pension.service.pensioner;

import com.v1.pension.exception.ResourceNotFoundException;
import com.v1.pension.model.pensioner.NidAndOtherDetail;
import com.v1.pension.repository.pensioner.NidAndOtherDetailRepository;
import com.v1.pension.repository.pensioner.PensionerRegistrationRepository;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NidAndOtherDetailService {
  private final NidAndOtherDetailRepository nidAndOtherDetailRepository;
  private final PensionerRegistrationRepository pensionerRegistrationRepository;

  public List<Tuple> getAllBySubPNo(String subPNo) {
    return nidAndOtherDetailRepository.getAllBySubPNo(subPNo);
  }

  public NidAndOtherDetail getById(long id) {
    return nidAndOtherDetailRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("NID And Other Data Not Found"));
  }

  public void savePensionerNidAndOtherDetails(NidAndOtherDetail request) {
    var pensioner = pensionerRegistrationRepository.findByPensionSubPNo(request.getPensionSubPNo())
        .orElseThrow(() -> new ResourceNotFoundException("Pensioner Not Found"));

    if (!request.getOtherTypeCode().equals("6") && request.getNid().isBlank())
      request.setNid("000000000000" + request.getOtherTypeCode());

    if (request.getStatus() == 1) {
      if (!request.getOtherTypeCode().isBlank()) pensioner.setOtherTypeCode(request.getOtherTypeCode());
      if (!request.getNid().isBlank()) pensioner.setNationalId(request.getNid());
      if (!request.getPan().isBlank()) pensioner.setPanNumber(request.getPan());
      pensionerRegistrationRepository.save(pensioner);
    }

    nidAndOtherDetailRepository.save(request);
  }
}
