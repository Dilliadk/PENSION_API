package com.v1.pension.dto.ipfms;

public interface IpfmsPaymentDetail {
  long getPensionPaymentDetailId();

  String getPan();

  String getPayeeCode();

  String getEname();

  String getNname();

  double getTotalAmount();

  double getSocialTaxAmount();

  double getTdsAmount();

  double getPayableAmount();

  long getBankId();

  String getBankAccountNo();
}
