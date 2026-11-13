package com.v1.pension.dto.pensioner.voucher_transaction;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VoucherTaxCalculationDto {
  private long masterId;
  private String pensionSubPNo;
  private double amount;
}
