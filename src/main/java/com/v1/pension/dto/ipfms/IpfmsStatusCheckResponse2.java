package com.v1.pension.dto.ipfms;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IpfmsStatusCheckResponse2 {
  private boolean success;
  private String message;
  private String jsonData;
}
