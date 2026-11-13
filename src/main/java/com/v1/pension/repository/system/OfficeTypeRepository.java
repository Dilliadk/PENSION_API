package com.v1.pension.repository.system;

import com.v1.pension.model.system.OfficeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OfficeTypeRepository extends JpaRepository<OfficeType, Integer> {
  @Query(nativeQuery = true, value = "select * from office_types where is_bank = true")
  List<OfficeType> getMainBank();
}
