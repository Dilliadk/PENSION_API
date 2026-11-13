package com.v1.pension.dto.system.office;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OfficeRequestDto {
  private int officeTypeId;
  private String officeCode;
  private String officeNameNep;
  private String officeNameEng;
  private String districtCode;
  private String remarks;
  private int status;
}
