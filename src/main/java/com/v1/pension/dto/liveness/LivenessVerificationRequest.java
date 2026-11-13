package com.v1.pension.dto.liveness;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LivenessVerificationRequest {
  private String sessionId;
  private String videoFrameBase64;
}
