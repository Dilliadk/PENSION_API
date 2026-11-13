package com.v1.pension.dto.nid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CitizenDataResponse {
  private CitizenDataForPublicServiceCommision citizenDataForPublicServiceCommision;
}

