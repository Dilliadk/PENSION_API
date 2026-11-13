package com.v1.pension.controller.auth;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.v1.pension.dto.auth.LivenessRegistrationRequestDto;
import com.v1.pension.dto.auth.LoginRequestDto;
import com.v1.pension.dto.auth.OtpRequestDto;
import com.v1.pension.dto.auth.OtpVerificationDto;
import com.v1.pension.dto.auth.RoleRequestDto;
import com.v1.pension.dto.auth.RoleResponseDto;
import com.v1.pension.exception.BadRequestException;
import com.v1.pension.service.user.AuthService;
import com.v1.pension.utils.MyResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;

  // @GetMapping("/change-third-party-password")
  // public ResponseEntity<Object> changeThirdPartyPassword() {
  //   authService.changeThirdPartyPassword("nagarik_app");
  //   return ResponseEntity.ok(MyResponse.message("done"));
  // }

  @PostMapping("/liveness-registration")
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public ResponseEntity<Object> livenessRegistration(@Valid @RequestBody LivenessRegistrationRequestDto dto) {
    var id = authService.livenessRegistration(dto);
    return ResponseEntity.ok(MyResponse.message("Registration Request Accepted, Please Verify Your OTP", id));
  }

  @PostMapping("/liveness-registration/{id}/otp-verification")
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public ResponseEntity<Object> verifyOtpForLivenessVerification(@PathVariable long id,
      @RequestBody OtpRequestDto dto) {
    authService.verifyOtpForLivenessVerification(id, dto);
    return ResponseEntity.ok(MyResponse.message("Registration Successful, please login"));
  }

  @PostMapping("/my-roles")
  public ResponseEntity<List<RoleResponseDto>> getMyRoles(@Valid @RequestBody RoleRequestDto dto) {
    var roles = authService.getUserRoles(dto);
    return ResponseEntity.ok(roles);
  }

  @PostMapping("/third-party/login")
  public ResponseEntity<Object> thirdPartyLogin(@Valid @RequestBody RoleRequestDto dto) {
    var token = authService.thirdPartyLogin(dto);
    return ResponseEntity.ok(Map.of("accessToken", token));
  }

  @GetMapping("/logout")
  public ResponseEntity<Object> logout(HttpServletRequest request) {
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, generateHttpOnlyCookie("", 0).toString())
        .build();
  }

  @PostMapping("/login")
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public ResponseEntity<?> login(
      @Valid @RequestBody LoginRequestDto loginRequest, HttpServletRequest request) {
    var disabledUsers = List.of("nagarik_app");
    if (disabledUsers.contains(loginRequest.getUsername()))
      throw new BadRequestException("Login Not Allowed");

    var ipAddress = getClientIpAddress(request);
    loginRequest.setIpAddress(ipAddress);

    var response = authService.login(loginRequest);
    if (response.isNewDevice()) {
      return ResponseEntity.ok(Map.of("newDevice", response.isNewDevice(), "message", response.getMessage()));
    } else {
      return ResponseEntity.ok()
          .header(HttpHeaders.SET_COOKIE, generateHttpOnlyCookie(response.getAccessToken(), 43200).toString())
          .body(Map.of("newDevice", response.isNewDevice(), "message", response.getMessage()));
    }
  }

  @PostMapping("/otp-verification")
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public ResponseEntity<?> verifyOtp(@Valid @RequestBody OtpVerificationDto otpRequest) {
    var response = authService.verifyOtpAndLogin(otpRequest);
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, generateHttpOnlyCookie(response.getAccessToken(), 43200).toString())
        .body(Map.of("newDevice", response.isNewDevice(), "message", response.getMessage()));
  }

  private ResponseCookie generateHttpOnlyCookie(String jwtToken, int maxAge) {
    return ResponseCookie.from("jwt_auth_cookie", jwtToken)
        .httpOnly(true) // Prevents JavaScript access (XSS protection)
        .secure(true) // Forces HTTPS
        .path("/") // Available across the whole app
        .maxAge(maxAge) // Cookie expiration in seconds (e.g., 12 hours)
        .sameSite("Lax") // Protects against CSRF
        .build();
  }

  private String getClientIpAddress(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
      return xForwardedFor.split(",")[0];
    }

    String xRealIp = request.getHeader("X-Real-IP");
    if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
      return xRealIp;
    }

    return request.getRemoteAddr();
  }
}