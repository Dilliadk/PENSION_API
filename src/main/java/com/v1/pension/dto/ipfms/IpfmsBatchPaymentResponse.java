package com.v1.pension.dto.ipfms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IpfmsBatchPaymentResponse {
  private boolean success;
  private String message;
}
