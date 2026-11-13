package com.v1.pension.service.system;

import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.v1.pension.dto.system.office_type.OfficeTypeRequestDto;
import com.v1.pension.exception.ResourceNotFoundException;
import com.v1.pension.model.system.OfficeType;
import com.v1.pension.repository.system.OfficeTypeRepository;

@Service
@RequiredArgsConstructor
public class OfficeTypeService {
  private final OfficeTypeRepository officeTypeRepository;
  private final ModelMapper modelMapper;

  public void addOfficeType(OfficeTypeRequestDto dto) {
    var entity = modelMapper.map(dto, OfficeType.class);
    officeTypeRepository.save(entity);
  }

  public OfficeType getById(int id) {
    return officeTypeRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Office Type Not Found"));
  }

  public void updateOfficeType(int id, OfficeTypeRequestDto dto) {
    var entity = getById(id);
    modelMapper.map(dto, entity);
    officeTypeRepository.save(entity);
  }
}
