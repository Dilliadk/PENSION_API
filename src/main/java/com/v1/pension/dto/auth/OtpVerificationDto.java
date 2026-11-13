package com.v1.pension.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OtpVerificationDto {
  @NotBlank(message = "OTP code is required")
  private String otpCode;

  @NotBlank(message = "Username is required")
  private String username;

  @NotNull(message = "RoleId Is Required")
  private int roleId;

  @NotBlank(message = "Office Code Is Required")
  private String officeCode;

  @NotBlank(message = "Fiscal Year is required")
  private String fiscalYear;

  private String deviceFingerprint;
}
