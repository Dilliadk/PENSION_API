package com.v1.pension.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;

import com.v1.pension.composite_keys.UserRoleMappingId;
import com.v1.pension.model.user.UserRoleMapping;

public interface UserRoleMappingRepository extends JpaRepository<UserRoleMapping, UserRoleMappingId> {
}
