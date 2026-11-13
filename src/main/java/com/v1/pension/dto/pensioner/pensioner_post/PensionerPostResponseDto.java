package com.v1.pension.dto.pensioner.pensioner_post;

public interface PensionerPostResponseDto {
  long getId();

  String getPensionerTypeCode();

  String getPensionerCategoryCode();

  String getPensionerLevelCode();

  String getPensionerPostCode();

  String getPostNameNep();

  String getPostNameEng();

  int getStatus();
}
