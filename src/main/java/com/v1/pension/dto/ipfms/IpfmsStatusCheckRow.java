package com.v1.pension.dto.ipfms;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IpfmsStatusCheckRow {
  private long sno;
  private String payeeCode;
  private String ename;
  private double payableAmount;
  private String eftNo;
  private String status;
  private String rejectReason;
  private String settlementDate;

  public int getStatusCode() {
    return switch (status) {
      case "Success" -> 3;
      case "Reject" -> 4;
      default -> 2;
    };
  }
}
