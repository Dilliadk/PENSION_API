package com.v1.pension.controller.system;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.v1.pension.dto.pensioner.pensioner_level.PensionerLevelRequestDto;
import com.v1.pension.service.system.PensionerLevelService;
import com.v1.pension.utils.MyResponse;

@RestController
@RequestMapping("/api/pensioner-levels")
@RequiredArgsConstructor
public class PensionerLevelController {
  private final PensionerLevelService levelService;

  @GetMapping("/all/details")
  public ResponseEntity<?> getAllPensionerLevelDetails() {
    var data = levelService.getAllPensionerLevelDetails();
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/active/summaries")
  public ResponseEntity<?> getActivePensionerLevelSummariesByType(@RequestParam String ptCode) {
    if (ptCode.equals("2"))
      ptCode = "1"; // प्रहरी र अनुसन्धान lai निजामति jastai treat garne
    var data = levelService.getActivePensionerLevelSummariesByType(ptCode);
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getById(@PathVariable long id) {
    var data = levelService.getResponseById(id);
    return ResponseEntity.ok(data);
  }

  @PostMapping
  public ResponseEntity<?> addPensionerLevel(@RequestBody PensionerLevelRequestDto dto) {
    levelService.addPensionerLevel(dto);
    return ResponseEntity.ok(MyResponse.message("Pensioner level has been saved"));
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> updatePensionerLevel(@PathVariable long id, @RequestBody PensionerLevelRequestDto dto) {
    levelService.updatePensionerLevel(id, dto);
    return ResponseEntity.ok(MyResponse.message("Pensioner level has been updated"));
  }
}
