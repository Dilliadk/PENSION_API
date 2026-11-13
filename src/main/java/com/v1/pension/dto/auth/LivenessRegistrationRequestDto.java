package com.v1.pension.dto.auth;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
public class LivenessRegistrationRequestDto {
  @NotBlank(message = "Pension Number Is Required")
  private String pensionNumber;

  @NotBlank(message = "National Id Is Required")
  private String nationalId;

  @Pattern(regexp = "^9[87]\\d{8}", message = "Invalid Mobile Number")
  private String mobileNumber;

  @Pattern(regexp = "^9[87]\\d{8}", message = "Invalid Mobile Number")
  private String confirmMobileNumber;

  @NotBlank(message = "Retired Date Is Required")
  private String retiredDate;
}
