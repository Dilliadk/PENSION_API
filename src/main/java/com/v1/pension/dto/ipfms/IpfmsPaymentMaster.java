package com.v1.pension.dto.ipfms;

import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class IpfmsPaymentMaster {
  private long pensionPaymentId;
  private String budYear;
  private String provncCode = "0";
  private long treasuryOfficeId = 78;
  private String agencyCode = "305013502";
  private String accountCode;
  private String economicCode;
  private String componentCode = "0";
  private String activityCode;
  private String priDonorCode = "1100001";
  private String sourceTypeCode = "";
  private String priSourceTypeCode = "01";
  private String secDonorCode = "";
  private String secSourceTypeCode = "";
  private String refBatchNo;
  private long categoryTypeId;
  private long paymentTypeId = 1;
  private String remarks = "";
  private long batchCode = 0;
  private long batchId = 0;
  private String tranDate = "";
  private int status = 0;
  private String createdDate;
  private List<Map<String, Object>> details;
}