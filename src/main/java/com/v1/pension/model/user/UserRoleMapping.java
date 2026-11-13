package com.v1.pension.model.user;

import com.v1.pension.composite_keys.UserRoleMappingId;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

@Data
@Entity
@Table(name = "user_role_mapping")
@IdClass(UserRoleMappingId.class)
public class UserRoleMapping {
  @Id
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Id
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "role_id")
  private UserRole role;

  @Id
  private String officeCode;

  @ColumnDefault("1")
  private int status = 1;
}
