package com.v1.pension.controller.other;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.v1.pension.exception.ResourceNotFoundException;
import com.v1.pension.repository.pensioner.IncomeCertificateRepository;
import com.v1.pension.utils.MyResponse;

@RestController
@RequestMapping("/api/income-certificates")
@RequiredArgsConstructor
public class IncomeCertificateController {
  private final IncomeCertificateRepository incomeCertificateRepository;

  @GetMapping
  public ResponseEntity<Object> getIncomeCertificatesForPensioner(@RequestParam String subPNo) {
    var data = incomeCertificateRepository.getIncomeCertificatesForPensioner(subPNo);
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Object> deleteIncomeCertificate(@PathVariable long id) {
    var entity = incomeCertificateRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Income Certificate Not Found"));

    incomeCertificateRepository.delete(entity);
    return ResponseEntity.ok(MyResponse.message("Income Certificate Has Been Deleted"));
  }
}
