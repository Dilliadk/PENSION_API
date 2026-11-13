package com.v1.pension.dto.pensioner.voucher_transaction;

public interface PensionerInfoForMonthlyGeneration {
  String getSubNumber();

  int getFestivalMonth();

  double getPensionAmountPerMonth();

  Double getPensionAmountPerMonthFromVoucher();

  boolean getLegalCase();

  double getTotalReceivable();

  // double getTotalPaidTax();

  String getOtherTypeCode();

  int getPensionMaritalStatus();

  String getPensionGender();

  String getPensionEndDateNep();

  String getCalculationFromDate();

  String getCalculationToDate();
}
