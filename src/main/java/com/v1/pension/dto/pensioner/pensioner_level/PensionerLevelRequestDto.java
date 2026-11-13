package com.v1.pension.dto.pensioner.pensioner_level;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PensionerLevelRequestDto {
  private String pensionerTypeCode;
  private String pensionerLevelCode;
  private String levelNameNep;
  private String levelNameEng;
  private int status;
}
