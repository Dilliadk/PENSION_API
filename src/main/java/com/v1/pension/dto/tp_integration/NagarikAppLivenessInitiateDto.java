package com.v1.pension.dto.tp_integration;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
public class NagarikAppLivenessInitiateDto {
  @NotBlank(message = "Pension No. is required")
  private String pensionNo;

  @NotBlank(message = "returnTo is required")
  private String returnTo;
}
