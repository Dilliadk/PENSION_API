package com.v1.pension.service.pensioner;

import com.v1.pension.exception.ResourceNotFoundException;
import com.v1.pension.model.pensioner.PensionerBlockRelease;
import com.v1.pension.repository.pensioner.PensionerBlockReleaseRepository;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PensionerBlockReleaseService {
  private final PensionerBlockReleaseRepository pensionerBlockReleaseRepository;

  public List<Tuple> getPensionerBlockReleases(String subPNo) {
    return pensionerBlockReleaseRepository.getAllForPensioner(subPNo);
  }

  public PensionerBlockRelease getById(long id) {
    return pensionerBlockReleaseRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Block/Release Info Not Found"));
  }

  public void saveBlockRelease(PensionerBlockRelease request) {
    pensionerBlockReleaseRepository.save(request);
  }
}
