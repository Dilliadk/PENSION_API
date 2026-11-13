package com.v1.pension.dto.tp_integration;

import java.time.LocalDate;

public interface PensionerInfoForNagarikApp {
  String getPensionerNameNep();

  String getPensionerNameEng();

  String getPensionerStatusNameNep();

  String getPensionerStatusNameEng();

  LocalDate getLastPresenceDateEng();

  String getLastPresenceDateNep();

  int getDaysSinceLastPresence();

  boolean getKycValid();

  LocalDate getNextPresenceDateEng();

  String getNextPresenceDateNep();
}
