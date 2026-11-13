package com.v1.pension.repository.system;

import com.v1.pension.model.system.Province;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProvinceRepository extends JpaRepository<Province, Long> {
  @Query(nativeQuery = true, value = "select id, code, name_nep as name from provinces")
  List<Tuple> getAllProvinces();
}
