package com.v1.pension.dto.liveness_prixa;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrixaFaceDetectionLog {
  private String created_at;

  @JsonProperty("is_verified")
  private boolean verified;

  private String age;
  private String gender;
  private String reason;

  @JsonProperty("force_next")
  private boolean forceNext;
}
