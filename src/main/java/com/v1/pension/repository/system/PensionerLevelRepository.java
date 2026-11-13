package com.v1.pension.repository.system;

import com.v1.pension.dto.pensioner.pensioner_level.PensionerLevelResponseDto;
import com.v1.pension.model.system.PensionerLevel;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PensionerLevelRepository extends JpaRepository<PensionerLevel, Long> {
  @Query(nativeQuery = true, value = """
      select pl.id, pt.type_name_nep, pl.pensioner_level_code,
      pl.level_name_nep, pl.level_name_eng, s.status_name_nep
      from pensioner_levels pl
      inner join pensioner_types pt on pt.type_code = pl.pensioner_type_code
      inner join status s on s.id = pl.status
      order by pl.pensioner_level_code
      """)
  List<Tuple> getAllPensionerLevelDetails();

  @Query(nativeQuery = true, value = """
      select id, pensioner_level_code as code, (pensioner_level_code || ' - ' || level_name_nep) as name
      from pensioner_levels
      where status = 1 and pensioner_type_code = :ptCode
      order by pensioner_level_code
      """)
  List<Tuple> getActivePensionerLevelSummariesByType(@Param("ptCode") String ptCode);

  @Query(nativeQuery = true, value = "select * from pensioner_levels where id = :id")
  Optional<PensionerLevelResponseDto> getResponseById(@Param("id") long id);

  boolean existsByPensionerLevelCode(String pensionerLevelCode);
}