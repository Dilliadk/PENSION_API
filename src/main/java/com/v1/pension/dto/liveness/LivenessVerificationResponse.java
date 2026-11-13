package com.v1.pension.dto.liveness;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LivenessVerificationResponse {
  @JsonProperty("isLive")
  private boolean isLive;
  private boolean facesMatch;
  private float confidence;
  private String message;
  private boolean success;
}
