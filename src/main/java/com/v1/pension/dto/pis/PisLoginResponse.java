package com.v1.pension.dto.pis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PisLoginResponse {
  private String token;
  private boolean success;
  private long user_id;
  private String server_cookie;
  private String message;
}
