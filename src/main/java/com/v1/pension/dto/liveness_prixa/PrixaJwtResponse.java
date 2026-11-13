package com.v1.pension.dto.liveness_prixa;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrixaJwtResponse {
  private String url;

  private String id;
}
