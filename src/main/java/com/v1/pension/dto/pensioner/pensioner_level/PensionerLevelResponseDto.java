package com.v1.pension.dto.pensioner.pensioner_level;

public interface PensionerLevelResponseDto {
  long getId();

  String getPensionerTypeCode();

  String getPensionerLevelCode();

  String getLevelNameNep();

  String getLevelNameEng();

  int getStatus();
}
