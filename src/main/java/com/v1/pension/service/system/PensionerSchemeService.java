package com.v1.pension.service.system;

import com.v1.pension.dto.pensioner.pensioner_scheme.PensionerSchemeRequestDto;
import com.v1.pension.dto.pensioner.pensioner_scheme.PensionerSchemeResponseDto;
import com.v1.pension.exception.BadRequestException;
import com.v1.pension.exception.ResourceNotFoundException;
import com.v1.pension.model.system.PensionerScheme;
import com.v1.pension.repository.system.PensionerSchemeRepository;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PensionerSchemeService {
  private final PensionerSchemeRepository schemeRepository;
  private final ModelMapper modelMapper;

  public List<Tuple> getAllPensionerSchemeDetails() {
    return schemeRepository.getAllPensionerSchemeDetails();
  }

  public List<Tuple> getActivePensionerSchemeSummariesByType(String ptCode) {
    return schemeRepository.getActivePensionerSchemeSummariesByType(ptCode);
  }

  public List<Tuple> getActivePensionerSchemeByShowType(String showType) {
    return schemeRepository.getActivePensionerSchemeByShowType(showType);
  }

  public List<Tuple> getActivePensionerSchemeSummaries() {
    return schemeRepository.getActivePensionerSchemeSummaries();
  }

  public PensionerSchemeResponseDto getResponseById(long id) {
    return schemeRepository.getResponseById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Pensioner scheme not found"));
  }

  private PensionerScheme getById(long id) {
    return schemeRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Pensioner scheme not found"));
  }

  public void addPensionerScheme(PensionerSchemeRequestDto dto) {
    if (schemeRepository.existsBySchemeCode(dto.getSchemeCode()))
      throw new BadRequestException("This scheme code already exists");

    var entity = modelMapper.map(dto, PensionerScheme.class);
    schemeRepository.save(entity);
  }

  public void updatePensionerScheme(long id, PensionerSchemeRequestDto dto) {
    var entity = getById(id);

    if (!Objects.equals(entity.getSchemeCode(), dto.getSchemeCode())) {
      if (schemeRepository.existsBySchemeCode(dto.getSchemeCode()))
        throw new BadRequestException("This scheme code already exists");
    }

    modelMapper.map(dto, entity);
    schemeRepository.save(entity);
  }
}
