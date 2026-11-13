package com.v1.pension.repository.user;

import com.v1.pension.model.user.UserGroup;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserGroupRepository extends JpaRepository<UserGroup, Integer> {
  @Query(nativeQuery = true, value = "select id, group_name_eng as name from user_groups where status = 1")
  List<Tuple> getActiveUserGroups();
}
