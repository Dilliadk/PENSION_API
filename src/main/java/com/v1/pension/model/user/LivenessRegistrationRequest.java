package com.v1.pension.model.user;

import java.time.LocalDateTime;
import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

@Data
@Entity
@Table(name = "liveness_registration_requests")
public class LivenessRegistrationRequest {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @CreationTimestamp
  private LocalDateTime createdAt;

  private String pensionNumber;

  private String nationalId;

  private String retiredDate;

  private String mobileNumber;

  @Column(length = 300)
  private String hashedOtp;

  private LocalDateTime otpValidUntil;

  private int status = 1; // 1 = new, 2 = registered
}
