package com.v1.pension.controller.system;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.v1.pension.model.system.PensionIncrement;
import com.v1.pension.service.pensioner.PensionerIncrementService;
import com.v1.pension.utils.MyResponse;

@RestController
@RequestMapping("/api/pensioner-increments")
@RequiredArgsConstructor
public class PensionerIncrementController {
  private final PensionerIncrementService pensionerIncrementService;

  @GetMapping
  public ResponseEntity<?> getAllByPost(@RequestParam String postCode) {
    var data = pensionerIncrementService.getAllByPost(postCode);
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getById(@PathVariable long id) {
    var data = pensionerIncrementService.getById(id);
    return ResponseEntity.ok(data);
  }

  @PostMapping
  public ResponseEntity<?> create(@RequestBody PensionIncrement request) {
    pensionerIncrementService.addIncrement(request);
    return ResponseEntity.ok(MyResponse.message("Pension Increment Has Been Saved"));
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> update(@PathVariable Long id, @RequestBody PensionIncrement inc) {
    pensionerIncrementService.updateIncrement(id, inc);
    return ResponseEntity.ok(MyResponse.message("Pension Increment Has Been Updated"));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> delete(@PathVariable Long id) {
    pensionerIncrementService.deleteIncrement(id);
    return ResponseEntity.ok(MyResponse.message("Pension Increment Has Been Deleted"));
  }
}