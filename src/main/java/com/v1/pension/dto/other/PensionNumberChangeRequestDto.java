package com.v1.pension.dto.other;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PensionNumberChangeRequestDto {
  private String oldValue;

  private String newValue;
}
