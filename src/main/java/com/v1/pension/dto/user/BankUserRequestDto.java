package com.v1.pension.dto.user;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
public class BankUserRequestDto {
  @NotBlank(message = "Full Name is required")
  private String fullName;

  private String officeCode;

  @Pattern(regexp = "^9[87]\\d{8}$", message = "Invalid Mobile Number")
  private String mobileNo;

  private String email;

  private int messageMedium;
}
