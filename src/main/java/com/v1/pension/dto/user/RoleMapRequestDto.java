package com.v1.pension.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RoleMapRequestDto {
  @NotNull(message = "UserId is required")
  private long userId;

  @NotBlank(message = "OfficeCode is required")
  private String officeCode;

  @NotNull(message = "RoleId is required")
  private int roleId;

  private int status;
}
