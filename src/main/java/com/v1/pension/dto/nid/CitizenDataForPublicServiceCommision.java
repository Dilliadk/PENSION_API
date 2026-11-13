package com.v1.pension.dto.nid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CitizenDataForPublicServiceCommision {
  private byte[] portraitImage;
  private byte[] portraitImageJpeg;
  private String firstName;
  private String middleName;
  private String lastName;
  private String firstNameLoc;
  private String middleNameLoc;
  private String lastNameLoc;
  private String dob;
  private String dobLoc;
  private String gender;
  private String permState;
  private String permDistrict;
  private String permRurMun;
  private String permWard;
  private String permVillageTol;
  private String ccNumber;
  private String ccIssuingDistrict;
  private String ccIssuingDate;
  private String gfFirstName;
  private String gfMiddleName;
  private String gfLastName;
  private String gmFirstName;
  private String gmMiddleName;
  private String gmLastName;
  private String permStateLoc;
  private String permDistrictLoc;
  private String permRurMunLoc;
  private String permWardLoc;
  private String permVillageTolLoc;
  private String ccNumberLoc;
  private String ccIssuingDistrictLoc;
  private String ccIssuingDateLoc;
  private String gfFirstNameLoc;
  private String gfMiddleNameLoc;
  private String gfLastNameLoc;
  private String gmFirstNameLoc;
  private String gmMiddleNameLoc;
  private String gmLastNameLoc;
  private String cast;
  private String castLoc;
  private String religion;
  private String religionLoc;
}
