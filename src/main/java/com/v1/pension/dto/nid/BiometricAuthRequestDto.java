package com.v1.pension.dto.nid;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BiometricAuthRequestDto {
  @NotBlank(message = "nin is required")
  private String nin;

  private String fingerprintPosition;

  private byte[] imageData;
}
