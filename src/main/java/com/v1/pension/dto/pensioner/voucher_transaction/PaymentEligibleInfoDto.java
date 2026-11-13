package com.v1.pension.dto.pensioner.voucher_transaction;

public interface PaymentEligibleInfoDto {
  String getSubNumber();

  double getBalance();

  String getCalculationFromDate();

  String getCalculationToDate();

  boolean getLegalCase();

  double getMonthlyAmount();

  String getLastMonthlyPensionDate();

  String getOtherTypeCode();

  int getPensionMaritalStatus();

  String getPensionGender();

  String getPensionEndDateNep();

  int getFestivalMonth();
}
