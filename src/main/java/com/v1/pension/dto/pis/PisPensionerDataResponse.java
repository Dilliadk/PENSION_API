package com.v1.pension.dto.pis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PisPensionerDataResponse {
  private List<PisPensionerPersonInfo> person_info;
  private List<PisPensionerPersonOtherInfo> person_other_info;
}
