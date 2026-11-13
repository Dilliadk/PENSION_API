package com.v1.pension.dto.pensioner.voucher_transaction;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DigitallySignedDocumentSearchDto {
  private String tableName;
  private String type;
  private long recordId;
}
