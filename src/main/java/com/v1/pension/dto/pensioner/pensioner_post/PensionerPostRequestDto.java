package com.v1.pension.dto.pensioner.pensioner_post;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PensionerPostRequestDto {
  private String pensionerCategoryCode;
  private String pensionerLevelCode;
  private String pensionerPostCode;
  private String postNameNep;
  private String postNameEng;
  private int status;
}
