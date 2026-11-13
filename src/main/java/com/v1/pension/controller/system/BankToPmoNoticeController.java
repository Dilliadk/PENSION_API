package com.v1.pension.controller.system;

import com.v1.pension.model.system.BankToPmoNotice;
import com.v1.pension.service.system.BankToPmoNoticeService;
import com.v1.pension.utils.MyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bank-to-pmo-notices")
@RequiredArgsConstructor
public class BankToPmoNoticeController {
  private final BankToPmoNoticeService bankToPmoNoticeService;

  @GetMapping
  public ResponseEntity<?> getActiveBankToPmoNotices() {
    var data = bankToPmoNoticeService.getActiveBankToPmoNoticeList();
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @PostMapping
  public ResponseEntity<?> saveBankToPmoNotice(@RequestBody BankToPmoNotice request) {
    bankToPmoNoticeService.saveBankToPmoNotice(request);
    return ResponseEntity.ok(MyResponse.message("Notice Has Been Sent To Pension Management Office"));
  }
}
