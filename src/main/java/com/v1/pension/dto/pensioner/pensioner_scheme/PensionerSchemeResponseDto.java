package com.v1.pension.dto.pensioner.pensioner_scheme;

public interface PensionerSchemeResponseDto {
  long getId();

  String getTypeCode();

  String getSchemeCode();

  String getSchemeNameNep();

  String getSchemeNameEng();

  int getStatus();
}
