package com.v1.pension.controller.system;

import com.v1.pension.dto.system.office.OfficeRequestDto;
import com.v1.pension.service.system.OfficeService;
import com.v1.pension.utils.MyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/offices")
@RequiredArgsConstructor
public class OfficeController {
  private final OfficeService officeService;

  @GetMapping("/office-types")
  public ResponseEntity<?> getAllOfficeTypes() {
    var data = officeService.getAllOfficeTypes();
    return ResponseEntity.ok(data);
  }

  @GetMapping("/main-banks")
  public ResponseEntity<?> getMainBank() {
    var data = officeService.getMainBank();
    return ResponseEntity.ok(data);
  }

  @GetMapping("/all/details")
  public ResponseEntity<?> getAllOffices() {
    var data = officeService.getAllOfficeDetails();
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/active/summaries")
  public ResponseEntity<?> getActiveOffices(@RequestParam(required = false) Integer type) {
    var data = officeService.getActiveOfficeSummaries(type);
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getOfficeById(@PathVariable long id) {
    var data = officeService.getOfficeResponseById(id);
    return ResponseEntity.ok(data);
  }

  @PostMapping
  public ResponseEntity<?> addOffice(@RequestBody OfficeRequestDto dto) {
    officeService.addOffice(dto);
    return ResponseEntity.ok(MyResponse.message("Office has been saved successfully"));
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> updateOffice(@PathVariable long id, @RequestBody OfficeRequestDto dto) {
    officeService.updateOffice(id, dto);
    return ResponseEntity.ok(MyResponse.message("Office has been updated successfully"));
  }
}
