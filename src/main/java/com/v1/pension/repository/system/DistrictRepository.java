package com.v1.pension.repository.system;

import com.v1.pension.model.system.District;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DistrictRepository extends JpaRepository<District, Long> {
  @Query(nativeQuery = true, value = """
          select D.id, code, name,s.status_name_nep as status from districts D
          inner join status s on s.id = D.status order by 2
          """)
  List<Tuple> getAllDistricts();

  @Query(nativeQuery = true, value = "select id, code, name from districts where province = :provinceCode")
  List<Tuple> getFilteredDistricts(@Param("provinceCode") String provinceCode);
}