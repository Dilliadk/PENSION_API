package com.v1.pension.dto.other;

public record JwtPayload(
    long userId,
    String userName,
    String deviceFingerprint,
    String tokenType,
    String role,
    String officeCode,
    String fiscalYear
) {
  public boolean isRoot() {
    return "ROOT".equals(role);
  }
}
