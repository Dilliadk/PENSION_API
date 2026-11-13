package com.v1.pension.controller.system;

import com.v1.pension.dto.pensioner.pensioner_scheme.PensionerSchemeRequestDto;
import com.v1.pension.service.system.PensionerSchemeService;
import com.v1.pension.utils.MyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pensioner-schemes")
@RequiredArgsConstructor
public class PensionerSchemeController {
  private final PensionerSchemeService schemeService;

  @GetMapping("/all/details")
  public ResponseEntity<?> getAllPensionerSchemeDetails() {
    var data = schemeService.getAllPensionerSchemeDetails();
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/active/summaries")
  public ResponseEntity<?> getActivePensionerSchemeSummaries() {
    var data = schemeService.getActivePensionerSchemeSummaries();
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/active/summaries/by-type")
  public ResponseEntity<?> getActivePensionerSchemeSummariesByType(@RequestParam String ptCode) {
    var data = schemeService.getActivePensionerSchemeSummariesByType(ptCode);
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/active/summaries/by-show-type")
  public ResponseEntity<?> getActivePensionerSchemeByShowType(@RequestParam String showType) {
    var data = schemeService.getActivePensionerSchemeByShowType(showType);
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getById(@PathVariable long id) {
    var data = schemeService.getResponseById(id);
    return ResponseEntity.ok(data);
  }

  @PostMapping
  public ResponseEntity<?> addPensionerScheme(@RequestBody PensionerSchemeRequestDto dto) {
    schemeService.addPensionerScheme(dto);
    return ResponseEntity.ok(MyResponse.message("Pensioner Scheme Has Been Saved"));
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> updatePensionerScheme(@PathVariable long id, @RequestBody PensionerSchemeRequestDto dto) {
    schemeService.updatePensionerScheme(id, dto);
    return ResponseEntity.ok(MyResponse.message("Pensioner Scheme Has Been Updated"));
  }
}
