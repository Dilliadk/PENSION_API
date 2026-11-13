package com.v1.pension.dto.pensioner.bank_account_close_information;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BankAccountCloseInformationUpdateDto {
  private String date;
  private double amount;
}
