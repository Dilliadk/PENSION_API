package com.v1.pension.controller.pensioner;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.v1.pension.dto.other.PensionNumberChangeRequestDto;
import com.v1.pension.service.pensioner.PensionNumberChangeLogService;
import com.v1.pension.utils.MyResponse;

@RestController
@RequestMapping("/api/pension-number-change-logs")
@RequiredArgsConstructor
public class PensionNumberChangeLogController {
  private final PensionNumberChangeLogService changeLogService;

  @GetMapping
  public ResponseEntity<Object> getPensionNumberChangeLogs() {
    var data = changeLogService.getPensionNumberChangeLogs();
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @PostMapping
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public ResponseEntity<Object> savePensionNumberChangeLog(@Valid @RequestBody PensionNumberChangeRequestDto dto) {
    changeLogService.changePensionNumber(dto);
    return ResponseEntity.ok(MyResponse.message("Pension Number Has Been Changed"));
  }
}
