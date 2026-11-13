package com.v1.pension.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.v1.pension.dto.auth.RoleResponseDto;
import com.v1.pension.dto.other.JwtPayload;
import com.v1.pension.model.user.User;
import com.v1.pension.repository.system.FiscalYearRepository;

@Component
public class JwtTokenUtil {
  private final SecretKey signingKey;
  private final FiscalYearRepository fyRepository;

  public JwtTokenUtil(@Value("${app.secret.key}") String secretKeyString, FiscalYearRepository fyRepository) {
    this.signingKey = Keys.hmacShaKeyFor(Base64.getEncoder().encode(secretKeyString.getBytes()));
    this.fyRepository = fyRepository;
  }

  public String generateAccessTokenForThirdPartyIntegration(User user, RoleResponseDto role) {
    var currentFy = fyRepository.getCurrentFiscalYear();
    var deviceFp = user.getUsername();
    long validFor = (long) 10 * 60 * 1000; // valid for 10 minutes
    return createToken(user, role.getRoleNameEng(), role.getOfficeCode(), currentFy.getFiscalYear(), "ACCESS", deviceFp,
        validFor);
  }

  public String generateAccessToken(User user, String role, String officeCode, String fy, String deviceFp) {
    long validFor = (long) 24 * 60 * 60 * 1000; // 24 hours
    return createToken(user, role, officeCode, fy, "ACCESS", deviceFp, validFor);
  }

  public String generateRefreshToken(User user, String role, String officeCode, String fy, String deviceFp) {
    long validFor = (long) 7 * 24 * 60 * 60 * 1000; // 7 days
    return createToken(user, role, officeCode, fy, "REFRESH", deviceFp, validFor);
  }

  private String createToken(User user, String role, String officeCode, String fy, String tokenType, String deviceFp,
      long validity) {
    var jwtBuilder = Jwts.builder()
        .setSubject(user.getUsername())
        .claim("role", role)
        .claim("deviceFingerprint", deviceFp)
        .claim("tokenType", tokenType)
        .claim("userId", user.getId())
        .claim("officeCode", officeCode)
        .claim("fiscalYear", fy)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + validity));

    return jwtBuilder.signWith(signingKey, SignatureAlgorithm.HS256).compact();
  }

  public Optional<JwtPayload> parseToken(String token) {
    try {
      Claims claims = Jwts.parserBuilder()
          .setSigningKey(signingKey)
          .build()
          .parseClaimsJws(token)
          .getBody();

      Long userId = claims.get("userId", Long.class);
      String userName = claims.getSubject();
      String role = claims.get("role", String.class);
      String deviceFingerprint = claims.get("deviceFingerprint", String.class);
      String tokenType = claims.get("tokenType", String.class);
      String officeCode = claims.get("officeCode", String.class);
      String fy = claims.get("fiscalYear", String.class);

      return Optional.of(new JwtPayload(userId, userName, deviceFingerprint, tokenType, role, officeCode, fy));
    } catch (ExpiredJwtException e) {
      return Optional.empty();
    } catch (Exception e) {
      return Optional.empty(); // Other validation errors
    }
  }
}
