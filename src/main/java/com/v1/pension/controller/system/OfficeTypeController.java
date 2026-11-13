package com.v1.pension.controller.system;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.v1.pension.dto.system.office_type.OfficeTypeRequestDto;
import com.v1.pension.model.system.OfficeType;
import com.v1.pension.service.system.OfficeTypeService;
import com.v1.pension.utils.MyResponse;

@RestController
@RequestMapping("/api/office-types")
@RequiredArgsConstructor
public class OfficeTypeController {
  private final OfficeTypeService officeTypeService;

  @PostMapping
  public ResponseEntity<?> addOfficeType(@Valid @RequestBody OfficeTypeRequestDto dto) {
    officeTypeService.addOfficeType(dto);
    return ResponseEntity.ok(MyResponse.message("Add Successful"));
  }

  @GetMapping("/{id}")
  public ResponseEntity<OfficeType> getById(@PathVariable int id) {
    var data = officeTypeService.getById(id);
    return ResponseEntity.ok(data);
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> updateOfficeType(@PathVariable int id, @Valid @RequestBody OfficeTypeRequestDto dto) {
    officeTypeService.updateOfficeType(id, dto);
    return ResponseEntity.ok(MyResponse.message("Update Successful"));
  }
}
