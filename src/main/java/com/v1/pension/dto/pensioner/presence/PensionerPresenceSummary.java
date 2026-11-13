package com.v1.pension.dto.pensioner.presence;

import java.time.LocalDate;

public interface PensionerPresenceSummary {
  String getPresenceDateNep();

  LocalDate getPresenceDateEng();

  String getPresenceType();

  Double getNidFaceMatchPercentage();
}
