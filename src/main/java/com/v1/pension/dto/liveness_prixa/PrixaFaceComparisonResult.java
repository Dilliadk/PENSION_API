package com.v1.pension.dto.liveness_prixa;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrixaFaceComparisonResult {
  @JsonProperty("SourceImageFace")
  private PrixaSourceImageFace sourceImageFace;

  @JsonProperty("FaceMatches")
  private List<PrixaFaceMatch> faceMatches;

  public double getSimilarityPercentage() {
    if (faceMatches == null || faceMatches.isEmpty())
      return 0;

    return faceMatches.get(0).getSimilarity();
  }
}
