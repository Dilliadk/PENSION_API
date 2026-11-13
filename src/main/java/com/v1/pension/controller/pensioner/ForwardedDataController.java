package com.v1.pension.controller.pensioner;

import com.v1.pension.dto.forward_data.ApproveDataDto;
import com.v1.pension.dto.forward_data.BackwardDataDto;
import com.v1.pension.dto.forward_data.ForwardDataDto;
import com.v1.pension.service.pensioner.ForwardedDataService;
import com.v1.pension.utils.MyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/forwarded-data")
@RequiredArgsConstructor
public class ForwardedDataController {
  private final ForwardedDataService forwardedDataService;

  @PostMapping("/forward")
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public ResponseEntity<Object> saveForwardData(@RequestBody ForwardDataDto dto) {
    forwardedDataService.saveForwardData(dto);
    return ResponseEntity.ok(MyResponse.message("Data Forward Successfully"));
  }

  @PostMapping("/backward")
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public ResponseEntity<Object> saveBackwardData(@RequestBody BackwardDataDto dto) {
    forwardedDataService.saveBackwardData(dto);
    return ResponseEntity.ok(MyResponse.message("Data Backward Successfully"));
  }

  @PostMapping("/approve")
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public ResponseEntity<Object> approveForm(@RequestBody ApproveDataDto dto) {
    forwardedDataService.approveForm(dto);
    return ResponseEntity.ok(MyResponse.message("Data Approved Successfully"));
  }
}
