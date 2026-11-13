package com.v1.pension.dto.pensioner.pensioner_scheme;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PensionerSchemeRequestDto {
  private String typeCode;
  private String schemeCode;
  private String schemeNameNep;
  private String schemeNameEng;
  private int status;
}
