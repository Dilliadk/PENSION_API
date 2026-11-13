package com.v1.pension.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequestDto {
  @NotBlank(message = "Username Is Required")
  private String username;

  @NotBlank(message = "Password Is Required")
  private String password;

  @NotNull(message = "RoleId Is Required")
  private int roleId;

  @NotBlank(message = "Office Code Is Required")
  private String officeCode;

  @NotBlank(message = "Fiscal Year is required")
  private String fiscalYear;

  private Boolean rememberMe = false;

  @NotBlank(message = "Device Fingerprint Is Required")
  private String deviceFingerprint;

  private String deviceName;

  private String browserInfo;

  private String ipAddress;
}
