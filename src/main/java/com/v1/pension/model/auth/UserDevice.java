package com.v1.pension.model.auth;

import com.v1.pension.model.BaseModel;
import com.v1.pension.model.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_devices")
@Data
@EqualsAndHashCode(callSuper = true)
public class UserDevice extends BaseModel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "device_fingerprint", nullable = false)
  private String deviceFingerprint;

  @Column(name = "device_name")
  private String deviceName;

  @Column(name = "browser_info")
  private String browserInfo;

  @Column(name = "ip_address")
  private String ipAddress;

  @Column(name = "is_trusted")
  private boolean trusted = false;

  @Column(name = "last_used")
  private LocalDateTime lastUsed;

  @Column(name = "refresh_token")
  private String refreshToken;

  @Column(name = "refresh_token_expiry")
  private LocalDateTime refreshTokenExpiry;
}
