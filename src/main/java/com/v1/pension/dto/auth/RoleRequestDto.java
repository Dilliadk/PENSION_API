package com.v1.pension.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RoleRequestDto {
  @NotBlank(message = "Username Is Required")
  private String username;

  @NotBlank(message = "Password Is Required")
  private String password;
}
