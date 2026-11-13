package com.v1.pension.service.pensioner;

import com.v1.pension.exception.ResourceNotFoundException;
import com.v1.pension.model.system.PensionIncrement;
import com.v1.pension.repository.system.PensionIncrementRepository;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PensionerIncrementService {
  private final PensionIncrementRepository pensionIncrementRepository;
  private final ModelMapper modelMapper;

  public List<Tuple> getAllByPost(String postCode) {
    return pensionIncrementRepository.getAllPensionIncrementByPost(postCode);
  }

  public PensionIncrement getById(long id) {
    return pensionIncrementRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Pension Increment Not Found"));
  }

  public void addIncrement(PensionIncrement request) {
    request.setStatus(1);
    pensionIncrementRepository.save(request);
  }

  public void updateIncrement(long id, PensionIncrement request) {
    var increment = getById(id);
    modelMapper.map(request, increment);
    pensionIncrementRepository.save(increment);
  }

  public void deleteIncrement(long id) {
    var increment = getById(id);
    pensionIncrementRepository.delete(increment);
  }
}
