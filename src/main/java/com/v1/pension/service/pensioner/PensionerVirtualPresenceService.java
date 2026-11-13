package com.v1.pension.service.pensioner;

import com.v1.pension.exception.ResourceNotFoundException;
import com.v1.pension.model.pensioner.PensionerVirtualPresence;
import com.v1.pension.repository.pensioner.PensionerRegistrationRepository;
import com.v1.pension.repository.pensioner.PensionerVirtualPresenceRepository;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PensionerVirtualPresenceService {
  private final PensionerRegistrationRepository pensionerRegistrationRepository;
  private final PensionerVirtualPresenceRepository pensionerVirtualPresenceRepository;

  public List<Tuple> getAllBySubPNo(String subPNo) {
    return pensionerVirtualPresenceRepository.getAllBySubPNo(subPNo);
  }

  public void addVirtualPresence(PensionerVirtualPresence request) throws IOException {
    var pensioner = pensionerRegistrationRepository.findByPensionSubPNo(request.getPensionSubPNo())
        .orElseThrow(() -> new ResourceNotFoundException("Pensioner With This SubPNo doesn't exist"));

    var file = request.getFaceImage();
    request.setFaceImageData(file.getBytes());

    pensionerVirtualPresenceRepository.save(request);
  }
}
