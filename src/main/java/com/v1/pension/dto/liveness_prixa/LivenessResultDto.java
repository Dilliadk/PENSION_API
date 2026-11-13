package com.v1.pension.dto.liveness_prixa;

import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
public class LivenessResultDto {
  private UUID id;
  private boolean faceValid;
  private boolean gestureValid;
  private double nidFaceMatchPercentage;
  private int status;

  @JsonProperty("result")
  boolean getResult() {
    if (isFaceValid() && isGestureValid() && nidFaceMatchPercentage > 80)
      return true;

    return false;
  }

  @JsonProperty("message")
  String getMessage() {
    if (status == 1 || status == 2)
      return "Liveness result is being processed, please wait for a moment";

    if (getResult()) {
      return "Liveness verification successful";
    } else {
      return "Liveness verification failed. Please try again";
    }
  }
}
