package com.v1.pension.dto.pensioner.voucher_transaction;

public record YearlyTaxableParametersDto(
    String pensionSubPNo,
    String otherTypeCode,
    int maritalStatus,
    int festivalMonth,
    Double receivedAmount,
    double monthlyAmount,
    Integer month,
    long masterId) {
}
