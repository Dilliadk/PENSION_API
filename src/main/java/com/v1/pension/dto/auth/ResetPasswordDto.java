package com.v1.pension.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

@Data
@NoArgsConstructor
public class ResetPasswordDto {
  @NotBlank(message = "Username is required")
  public String userName;

  @Range(min = 1, max = 2, message = "Invalid message medium")
  private int messageMedium;

  @NotBlank(message = "Mobile/Email Is Required")
  public String mobileOrEmail;
}
