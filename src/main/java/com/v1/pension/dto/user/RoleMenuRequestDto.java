package com.v1.pension.dto.user;

import com.v1.pension.model.user.RoleMenuMapping;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class RoleMenuRequestDto {
  @NotNull(message = "RoleId is required")
  private int roleId;

  @NotNull(message = "Menu List For Mapping Is Required")
  private List<RoleMenuMapping> menu;
}
