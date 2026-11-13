package com.v1.pension.dto.other;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReportDto {
  private Long recordId;
  private String reportName;
  private String pensionSubPNo;
  private int liabilityType;
  private String dateNep;
  private String bodartha;
  private String note;
  private String categoryCode;
  private String calculationTypeCode;
  private String dateFrom;
  private String dateTo;
  private int mainBankId;
  private String bankBranchCode;
  private String provinceCode;
  private String districtCode;
  private String fy;
  private String currencyCode;
  private String searchLabel1;
  private String searchValue1;
  private String searchLabel2;
  private String searchValue2;
  private String reportFormat = "pdf";
}
