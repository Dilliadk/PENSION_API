package com.v1.pension.dto.pensioner.voucher_transaction;

import com.v1.pension.model.pensioner.VoucherTransactionDetail;
import com.v1.pension.model.pensioner.VoucherTransactionMaster;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class VoucherCalculationDto {
  private VoucherTransactionMaster master;
  private List<VoucherTransactionDetail> details;
}
