package com.v1.pension.dto.nid;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NinRequestDto {
  @NotBlank(message = "NIN is required")
  private String nin;
}
