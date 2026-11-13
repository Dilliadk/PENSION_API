package com.v1.pension.dto.liveness_prixa;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
public class PrixaFaceMatch {
  @JsonProperty("Similarity")
  private double similarity;

  @JsonProperty("Face")
  private PrixaSourceImageFace face;
}
