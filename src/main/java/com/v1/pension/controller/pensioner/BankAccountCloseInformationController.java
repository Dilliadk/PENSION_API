package com.v1.pension.controller.pensioner;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.v1.pension.dto.pensioner.bank_account_close_information.BankAccountCloseInformationUpdateDto;
import com.v1.pension.service.pensioner.BankAccountCloseInformationService;
import com.v1.pension.utils.MyResponse;

@RestController
@RequestMapping("/api/bank-account-close-information")
@RequiredArgsConstructor
public class BankAccountCloseInformationController {
  private final BankAccountCloseInformationService bankAccountCloseInformationService;

  @GetMapping
  public ResponseEntity<Object> getInformation() {
    var data = bankAccountCloseInformationService.getInformation();
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @PutMapping("/{id}")
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public ResponseEntity<Object> updateAccountCloseInformation(@PathVariable long id,
      @RequestBody BankAccountCloseInformationUpdateDto dto) {
    bankAccountCloseInformationService.updateAccountCloseInformation(id, dto);
    return ResponseEntity.ok(MyResponse.message("Update Successful"));
  }
}
