package com.v1.pension.model.auth;

import com.v1.pension.model.BaseModel;
import com.v1.pension.model.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_verifications")
@Data
@EqualsAndHashCode(callSuper = true)
public class OtpVerification extends BaseModel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "otp_code", nullable = false)
  private String otpCode;

  @Column(name = "device_fingerprint")
  private String deviceFingerprint;

  @Column(name = "otp_type")
  private String otpType; // LOGIN, DEVICE_VERIFICATION, etc.

  @Column(name = "is_verified")
  private Boolean verified = false;

  @Column(name = "expiry_date", nullable = false)
  private LocalDateTime expiryDate;

  @Column(name = "attempts")
  private Integer attempts = 0;

  @Column(name = "max_attempts")
  private Integer maxAttempts = 3;

  private int status = 1;
}
