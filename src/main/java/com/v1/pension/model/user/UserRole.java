package com.v1.pension.model.user;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user_roles")
public class UserRole {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_group_id", nullable = false)
  private UserGroup group;

  private String roleNameNep;

  private String roleNameEng;

  private int status;
}
