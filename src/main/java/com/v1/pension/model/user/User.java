package com.v1.pension.model.user;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.ColumnDefault;

import com.v1.pension.model.BaseModel;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "users")
public class User extends BaseModel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_group_id", nullable = false)
  private UserGroup group;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id")
  private UserPost post;

  @Column(nullable = false, unique = true, length = 50)
  private String username;

  @Column(name = "full_name", nullable = false, length = 100)
  private String fullName;

  @Column(nullable = false, length = 100)
  private String officeCode;

  @Column(nullable = false, length = 15)
  private String mobileNo;

  @Column(nullable = false, unique = true, length = 100)
  private String email;

  @Column(nullable = false, length = 20)
  private String validUpto;

  @Column(nullable = false, length = 100)
  private String passwordHash; // hashed password

  private LocalDateTime lastLogin;

  @ColumnDefault("1")
  private int messageMedium = 1; // 1 = SMS, 2 = Email

  @Column(nullable = false)
  private int status = 1;
}
