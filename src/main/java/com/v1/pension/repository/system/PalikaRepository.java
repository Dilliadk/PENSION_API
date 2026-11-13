package com.v1.pension.repository.system;

import com.v1.pension.model.system.Palika;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PalikaRepository extends JpaRepository<Palika, Long> {
  @Query(nativeQuery = true, value = "select id, code, name from palikas where district_code = :districtCode")
  List<Tuple> getPalikaByDistrict(@Param("districtCode") String districtCode);
}
