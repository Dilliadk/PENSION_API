package com.v1.pension.dto.system.office;

public interface OfficeResponseDto {
  long getId();

  int getOfficeTypeId();

  String getOfficeCode();

  String getOfficeNameNep();

  String getOfficeNameEng();

  String getDistrictCode();

  String getRemarks();

  int getStatus();
}
