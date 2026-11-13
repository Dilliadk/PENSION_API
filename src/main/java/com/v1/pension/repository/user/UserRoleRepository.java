package com.v1.pension.repository.user;

import com.v1.pension.model.user.UserRole;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRoleRepository extends JpaRepository<UserRole, Integer> {
  @Query(nativeQuery = true, value = "select * from user_roles where role_name_eng = :roleName")
  Optional<UserRole> getRoleByName(@Param("roleName") String roleName);

  @Query(nativeQuery = true, value = """
      select id, role_name_eng as name
      from user_roles
      where status = 1 and (:groupId = -1 or user_group_id = :groupId)
      """)
  List<Tuple> getActiveRoles(@Param("groupId") int groupId);

  @Query(nativeQuery = true, value = "select user_group_id from user_roles where role_name_eng = :role")
  int getUserGroupIdForRole(String role);
}
