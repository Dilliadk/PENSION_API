package com.v1.pension.model.user;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user_groups")
public class UserGroup {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  private String groupNameNep;

  private String groupNameEng;

  private int status;
}
