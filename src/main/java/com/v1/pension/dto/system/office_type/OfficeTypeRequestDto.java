package com.v1.pension.dto.system.office_type;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
public class OfficeTypeRequestDto {
  @NotBlank(message = "Name is required")
  private String name;

  private String description;

  private Integer bankId;

  private boolean bank = true;
}
