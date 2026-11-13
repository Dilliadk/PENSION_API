package com.v1.pension.dto.auth;

import lombok.Data;

@Data
public class LoginResponseDto {
  private String message;
  private String accessToken;
  private String refreshToken;
  private boolean newDevice;
}

