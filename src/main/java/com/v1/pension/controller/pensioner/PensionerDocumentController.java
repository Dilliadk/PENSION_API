package com.v1.pension.controller.pensioner;

import java.io.IOException;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.v1.pension.exception.BadRequestException;
import com.v1.pension.model.pensioner.PensionerDocument;
import com.v1.pension.service.pensioner.PensionerDocumentService;
import com.v1.pension.utils.MyResponse;

@RestController
@RequestMapping("/api/pensioner-documents")
@RequiredArgsConstructor
public class PensionerDocumentController {
  private final PensionerDocumentService pensionerDocumentService;

  @GetMapping
  public ResponseEntity<?> getDocumentsForPensioner(@RequestParam String subPNo) {
    var data = pensionerDocumentService.getDocumentsForPensioner(subPNo);
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public ResponseEntity<Object> addPensionerDocument(@ModelAttribute PensionerDocument request) {
    try {
      pensionerDocumentService.addPensionerDocument(request);
      return ResponseEntity.ok(MyResponse.message("Pensioner Document Has Been Uploaded"));
    } catch (IOException e) {
      throw new BadRequestException("Failed to upload pensioner document");
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Object> deletePensionerDocument(@PathVariable long id) {
    pensionerDocumentService.deletePensionerDocument(id);
    return ResponseEntity.ok(MyResponse.message("Document Has Been Deleted Successfully"));
  }

  @GetMapping("/{id}/view")
  public ResponseEntity<byte[]> viewPensionerDocumentById(@PathVariable Long id) {

    return pensionerDocumentService.getPensionerDocumentById(id)
        .map(document -> ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(document.getContentType()))
            // .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFileName() + "\"")
            .header(HttpHeaders.CONTENT_DISPOSITION, generateContentDisposition(document.getFileName()).toString())
            .body(document.getFileData()))
        .orElse(ResponseEntity.notFound().build());
  }

  private ContentDisposition generateContentDisposition(String fileName) {
    return ContentDisposition
        .builder("attachment")
        .filename(fileName, java.nio.charset.StandardCharsets.UTF_8)
        .build();
  }
}
