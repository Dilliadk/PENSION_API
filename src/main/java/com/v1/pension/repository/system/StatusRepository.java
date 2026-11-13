package com.v1.pension.repository.system;

import com.v1.pension.model.system.Status;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StatusRepository extends JpaRepository<Status, Integer> {
  @Query(nativeQuery = true, value = "select id, status_name_nep as name from status")
  List<Tuple> getStatusList();
}
