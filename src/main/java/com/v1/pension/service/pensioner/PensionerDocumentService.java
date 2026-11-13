package com.v1.pension.service.pensioner;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

import jakarta.persistence.Tuple;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.v1.pension.exception.ResourceNotFoundException;
import com.v1.pension.model.pensioner.PensionerDocument;
import com.v1.pension.repository.pensioner.PensionerDocumentRepository;

@Service
@RequiredArgsConstructor
public class PensionerDocumentService {
  private final PensionerDocumentRepository pensionerDocumentRepository;

  public List<Tuple> getDocumentsForPensioner(String subPNo) {
    return pensionerDocumentRepository.getDocumentsForPensioner(subPNo);
  }

  public void addPensionerDocument(PensionerDocument request) throws IOException {
    var file = request.getFile();

    request.setFileName(file.getOriginalFilename());
    request.setFileType(file.getContentType());
    request.setContentType(file.getContentType());
    request.setFileData(file.getBytes());

    pensionerDocumentRepository.save(request);
  }

  public Optional<PensionerDocument> getPensionerDocumentById(long id) {
    return pensionerDocumentRepository.findById(id);
  }

  @PreAuthorize("hasAuthority('ROOT')")
  public void deletePensionerDocument(long id) {
    var document = pensionerDocumentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Document Not Found"));

    pensionerDocumentRepository.delete(document);
  }
}
