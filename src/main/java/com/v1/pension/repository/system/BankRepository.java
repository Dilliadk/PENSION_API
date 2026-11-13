package com.v1.pension.repository.system;

import java.util.List;

import jakarta.persistence.Tuple;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.v1.pension.model.system.BankEntity;

public interface BankRepository extends JpaRepository<BankEntity, Integer> {
  @Query(nativeQuery = true, value = "select id, code, name from banks where status = 1 order by name")
  List<Tuple> getActiveBanks();
}
