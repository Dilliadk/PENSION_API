package com.v1.pension.dto.pensioner.voucher_transaction;

public interface TameliPaymentInfo {
  String getPensionNumber();

  double getBalance();

  String getOtherTypeCode();

  int getPensionMaritalStatus();

  String getPensionGender();

  boolean getLegalCase();

  String getNominee();

  Integer getNomineeBankCode();

  String getNomineeBankAccountNo();

  String getPayeeCode();
}
