package com.v1.pension.dto.pensioner.voucher_transaction;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DigitallySignedDocumentRequestDto {
  private String tableName;
  private String type;
  private long recordId;
  private String data;
}
