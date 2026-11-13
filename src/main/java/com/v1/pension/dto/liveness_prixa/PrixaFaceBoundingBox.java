package com.v1.pension.dto.liveness_prixa;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrixaFaceBoundingBox {
  @JsonProperty("Width")
  private double width;

  @JsonProperty("Height")
  private double height;

  @JsonProperty("Left")
  private double left;

  @JsonProperty("Top")
  private double top;
}
