package com.v1.pension.dto.pis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PisPensionerPersonOtherInfo {
  private String nominee_full_name;
  private String nominee_full_name_en;
  private String nominee_relation;
  private String nominee_district;
  private String nominee_vdc;
  private String nominee_province;
  private String nominee_fed_ward_no;
  private String permanent_ward_no;
}
