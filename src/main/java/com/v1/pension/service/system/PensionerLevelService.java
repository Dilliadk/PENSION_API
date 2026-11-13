package com.v1.pension.service.system;

import com.v1.pension.dto.pensioner.pensioner_level.PensionerLevelRequestDto;
import com.v1.pension.dto.pensioner.pensioner_level.PensionerLevelResponseDto;
import com.v1.pension.exception.BadRequestException;
import com.v1.pension.exception.ResourceNotFoundException;
import com.v1.pension.model.system.PensionerLevel;
import com.v1.pension.repository.system.PensionerLevelRepository;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PensionerLevelService {
  private final PensionerLevelRepository levelRepository;
  private final ModelMapper modelMapper;

  public List<Tuple> getAllPensionerLevelDetails() {
    return levelRepository.getAllPensionerLevelDetails();
  }

  public List<Tuple> getActivePensionerLevelSummariesByType(String ptCode) {
    return levelRepository.getActivePensionerLevelSummariesByType(ptCode);
  }

  public PensionerLevelResponseDto getResponseById(long id) {
    return levelRepository.getResponseById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Pensioner Level Not Found"));
  }

  private PensionerLevel getById(long id) {
    return levelRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Pensioner Level Not Found"));
  }

  public void addPensionerLevel(PensionerLevelRequestDto dto) {
    if (levelRepository.existsByPensionerLevelCode(dto.getPensionerLevelCode()))
      throw new BadRequestException("This Pensioner Level Code Already Exists");

    var entity = modelMapper.map(dto, PensionerLevel.class);
    levelRepository.save(entity);
  }

  public void updatePensionerLevel(long id, PensionerLevelRequestDto dto) {
    var entity = getById(id);

    if (!Objects.equals(entity.getPensionerLevelCode(), dto.getPensionerLevelCode())) {
      if (levelRepository.existsByPensionerLevelCode(dto.getPensionerLevelCode()))
        throw new BadRequestException("This Pensioner Level Code Already Exists");
    }

    modelMapper.map(dto, entity);
    levelRepository.save(entity);
  }
}
