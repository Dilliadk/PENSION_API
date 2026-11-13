package com.v1.pension.service.system;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;

import jakarta.persistence.Tuple;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.v1.pension.dto.system.office.OfficeRequestDto;
import com.v1.pension.dto.system.office.OfficeResponseDto;
import com.v1.pension.exception.BadRequestException;
import com.v1.pension.exception.ResourceNotFoundException;
import com.v1.pension.model.system.Office;
import com.v1.pension.model.system.OfficeType;
import com.v1.pension.repository.system.OfficeRepository;
import com.v1.pension.repository.system.OfficeTypeRepository;
import com.v1.pension.utils.CurrentUser;

@Service
@RequiredArgsConstructor
public class OfficeService {
  private final OfficeRepository officeRepository;
  private final OfficeTypeRepository officeTypeRepository;
  private final ModelMapper modelMapper;

  public List<OfficeType> getAllOfficeTypes() {
    return officeTypeRepository.findAll();
  }

  public List<OfficeType> getMainBank() {
    return officeTypeRepository.getMainBank();
  }

  public List<Tuple> getAllOfficeDetails() {
    return officeRepository.getAllOfficeDetails();
  }

  public List<Tuple> getActiveOfficeSummaries(Integer type) {
    if (type == null && CurrentUser.getUserRole().equals("BANK_ADMIN")) {
      var office = officeRepository.findByOfficeCode(CurrentUser.getOfficeCode())
          .orElseThrow(() -> new ResourceNotFoundException("Office Not Found"));
      type = office.getType().getId();
    } else if (type == null) {
      type = -1; // -1 => 'All'
    }

    return officeRepository.getActiveOfficeSummaries(type);
  }

  private Office getById(long id) {
    return officeRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Office not found"));
  }

  public OfficeResponseDto getOfficeResponseById(long id) {
    return officeRepository.getOfficeResponseById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Office not found"));
  }

  public void addOffice(OfficeRequestDto dto) {
    // duplicate office code check
    var officeDup = officeRepository.findByOfficeCode(dto.getOfficeCode());
    if (officeDup.isPresent())
      throw new BadRequestException("This office code already exists");

    var office = modelMapper.map(dto, Office.class);
    office.setType(officeTypeRepository.getReferenceById(dto.getOfficeTypeId()));

    officeRepository.save(office);
  }

  public void updateOffice(long id, OfficeRequestDto dto) {
    var office = getById(id);

    // duplicate office code check
    if (!Objects.equals(office.getOfficeCode(), dto.getOfficeCode())) {
      var officeDup = officeRepository.findByOfficeCode(dto.getOfficeCode());
      if (officeDup.isPresent())
        throw new BadRequestException("This office code already exists");
    }

    modelMapper.map(dto, office);
    office.setType(officeTypeRepository.getReferenceById(dto.getOfficeTypeId()));
    officeRepository.save(office);
  }
}
