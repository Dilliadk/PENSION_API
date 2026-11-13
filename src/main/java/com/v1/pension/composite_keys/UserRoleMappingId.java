package com.v1.pension.composite_keys;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
public class UserRoleMappingId implements Serializable {
  private Long user;
  private Integer role;
  private String officeCode;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UserRoleMappingId that = (UserRoleMappingId) o;
    return Objects.equals(user, that.user)
        && Objects.equals(role, that.role)
        && Objects.equals(officeCode, that.officeCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(user, role, officeCode);
  }
}
