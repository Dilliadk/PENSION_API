package com.v1.pension.controller.pensioner;

import java.util.Map;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.v1.pension.dto.pensioner.voucher_transaction.DigitallySignedDocumentRequestDto;
import com.v1.pension.dto.pensioner.voucher_transaction.DigitallySignedDocumentSearchDto;
import com.v1.pension.service.pensioner.DigitallySignedDocumentService;
import com.v1.pension.utils.MyResponse;

@RestController
@RequestMapping("/api/digital-signature-documents")
@RequiredArgsConstructor
public class DigitallySignedDocumentController {
  private final DigitallySignedDocumentService digitallySignedDocumentService;

  @PostMapping("/exists")
  public ResponseEntity<Object> hasSignedDocument(@RequestBody DigitallySignedDocumentSearchDto dto) {
    var hasSignedLetter = digitallySignedDocumentService.existsByTypeTableNameAndRecordId(dto);

    var output = Map.of("hasSignedLetter", hasSignedLetter);
    return ResponseEntity.ok(output);
  }

  @PostMapping("/data")
  public ResponseEntity<Object> getSignedDocumentData(@RequestBody DigitallySignedDocumentSearchDto dto) {
    var data = digitallySignedDocumentService.getData(dto);
    return ResponseEntity.ok(data);
  }

  @PostMapping("/save")
  public ResponseEntity<Object> saveDigitallySignedDocument(@RequestBody DigitallySignedDocumentRequestDto dto) {
    digitallySignedDocumentService.saveDigitallySignedDocument(dto);
    return ResponseEntity.ok((MyResponse.message("Document Has Been Saved")));
  }
}
