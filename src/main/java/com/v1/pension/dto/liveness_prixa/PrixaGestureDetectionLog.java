package com.v1.pension.dto.liveness_prixa;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrixaGestureDetectionLog {
  private String created_at;

  @JsonProperty("is_verified")
  private boolean verified;

  private String challenged_gesture;
  private String detected_gesture;

  private double percentage_match;

  private String reason;

  @JsonProperty("is_passive_live")
  private boolean passiveLive;

  private double passive_score;
}
