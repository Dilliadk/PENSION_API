package com.v1.pension.dto.liveness_prixa;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
public class PrixaImageComparisonRequest {
  @JsonProperty("SourceImage")
  private BytesRequest sourceImage;

  @JsonProperty("TargetImage")
  private BytesRequest targetImage;

  @JsonProperty("SimilarityThreshold")
  private double similarityThreshold;
}
