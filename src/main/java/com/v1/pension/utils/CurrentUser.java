package com.v1.pension.utils;

import com.v1.pension.dto.other.JwtPayload;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class CurrentUser {
  private CurrentUser() {
  }

  private static Optional<JwtPayload> getJwtPayload() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof JwtPayload) {
      return Optional.of((JwtPayload) authentication.getPrincipal());
    }
    return Optional.empty();
  }

  public static long getUserId() {
    return getJwtPayload().map(JwtPayload::userId).orElse(0L);
  }

  public static String getUserName() {
    return getJwtPayload().map(JwtPayload::userName).orElse("");
  }

  public static String getUserRole() {
    return getJwtPayload().map(JwtPayload::role).orElse("");
  }

  public static String getOfficeCode() {
    return getJwtPayload().map(JwtPayload::officeCode).orElse("");
  }

  public static String getFiscalYear() {
    return getJwtPayload().map(JwtPayload::fiscalYear).orElse("");
  }
}
