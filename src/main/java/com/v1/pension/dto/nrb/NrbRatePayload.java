package com.v1.pension.dto.nrb;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NrbRatePayload {
  private List<NrbRate> rates;
}
