package com.v1.pension.controller.other;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.v1.pension.repository.report.LegacyDataRepository;

@RestController
@RequestMapping("/api/legacy-data")
@RequiredArgsConstructor
public class LegacyDataController {
  private final LegacyDataRepository legacyDataRepository;

  @GetMapping("/pensioner-documents")
  public ResponseEntity<Object> getPensionerDocuments(@RequestParam String subPNo) {
    var data = legacyDataRepository.getPensionerDocuments(subPNo);
    return ResponseEntity.ok(data);
  }

  @GetMapping("/pensioner-documents/{id}/view")
  public ResponseEntity<Object> viewDocument(@PathVariable long id) {
    var data = legacyDataRepository.getPensionerDocumentById(id);

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType((String) data.get("type")))
        .header(HttpHeaders.CONTENT_DISPOSITION, generateContentDisposition("document").toString())
        .body(data.get("sign"));
  }

  private ContentDisposition generateContentDisposition(String fileName) {
    return ContentDisposition
        .builder("attachment")
        .filename(fileName, java.nio.charset.StandardCharsets.UTF_8)
        .build();
  }
}
