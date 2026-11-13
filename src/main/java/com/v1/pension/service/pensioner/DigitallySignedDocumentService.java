package com.v1.pension.service.pensioner;

import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.v1.pension.dto.pensioner.voucher_transaction.DigitallySignedDocumentRequestDto;
import com.v1.pension.dto.pensioner.voucher_transaction.DigitallySignedDocumentSearchDto;
import com.v1.pension.exception.BadRequestException;
import com.v1.pension.exception.ResourceNotFoundException;
import com.v1.pension.model.pensioner.DigitallySignedDocument;
import com.v1.pension.repository.pensioner.DigitallySignedDocumentRepository;
import com.v1.pension.repository.pensioner.ForwardedDataRepository;
import com.v1.pension.repository.pensioner.VoucherTransactionMasterRepository;
import com.v1.pension.utils.CurrentUser;

@Service
@RequiredArgsConstructor
public class DigitallySignedDocumentService {
  private final DigitallySignedDocumentRepository digitallySignedDocumentRepository;
  private final VoucherTransactionMasterRepository voucherTransactionMasterRepository;
  private final ForwardedDataRepository forwardedDataRepository;
  private final ModelMapper modelMapper;

  public boolean existsByTypeTableNameAndRecordId(DigitallySignedDocumentSearchDto dto) {
    return digitallySignedDocumentRepository.existsByTypeTableNameAndRecordId(dto.getType(), dto.getTableName(),
        dto.getRecordId());
  }

  public DigitallySignedDocument getData(DigitallySignedDocumentSearchDto dto) {
    return digitallySignedDocumentRepository
        .findByTypeAndTableNameAndRecordId(dto.getType(), dto.getTableName(), dto.getRecordId())
        .orElseThrow(() -> new ResourceNotFoundException("Digitally signed letter not found"));
  }

  public void saveDigitallySignedDocument(DigitallySignedDocumentRequestDto dto) {
    if (digitallySignedDocumentRepository.existsByTypeTableNameAndRecordId(dto.getType(), dto.getTableName(),
        dto.getRecordId()))
      throw new BadRequestException("Digital signature already exists");

    if (dto.getTableName().equals("voucher_transaction_master")) {
      var voucher = voucherTransactionMasterRepository.findById(dto.getRecordId())
          .orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));
      if (voucher.getStage() != 4)
        throw new BadRequestException("Voucher has not been approved yet");

      var fd = forwardedDataRepository.findById(voucher.getForwardId())
          .orElseThrow(() -> new ResourceNotFoundException("Forward info not found"));

      if (fd.getReceiveUserId() != null && !fd.getReceiveUserId().equals(CurrentUser.getUserId()))
        throw new BadRequestException("This voucher has not been approved by you");
    }

    var entity = modelMapper.map(dto, DigitallySignedDocument.class);
    digitallySignedDocumentRepository.save(entity);
  }
}
