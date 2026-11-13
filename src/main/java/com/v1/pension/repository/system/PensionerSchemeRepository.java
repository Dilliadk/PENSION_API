package com.v1.pension.repository.system;

import com.v1.pension.dto.pensioner.pensioner_scheme.PensionerSchemeResponseDto;
import com.v1.pension.model.system.PensionerScheme;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PensionerSchemeRepository extends JpaRepository<PensionerScheme, Long> {
  @Query(nativeQuery = true, value = """
      select ps.id, pt.type_name_nep, ps.scheme_code, ps.increment_percent,
      ps.scheme_name_nep, ps.scheme_name_eng, s.status_name_nep
      from pensioner_schemes ps
      inner join pensioner_types pt on pt.type_code = ps.type_code
      inner join status s on s.id = ps.status
      order by ps.scheme_code
      """)
  List<Tuple> getAllPensionerSchemeDetails();

  @Query(nativeQuery = true, value = """
      select id, scheme_code as code, increment_percent, (scheme_code || ' - ' || scheme_name_nep) as name
      from pensioner_schemes
      where status = 1 and type_code = :ptCode
      order by scheme_code
      """)
  List<Tuple> getActivePensionerSchemeSummariesByType(@Param("ptCode") String ptCode);

  @Query(nativeQuery = true, value = """
      select id, scheme_code as code, increment_percent, (scheme_code || ' - ' || scheme_name_nep) as name
      from pensioner_schemes
      where status = 1
      order by scheme_code
      """)
  List<Tuple> getActivePensionerSchemeSummaries();

  @Query(nativeQuery = true, value = """
      select id, scheme_code as code, increment_percent, (scheme_code || ' - ' || scheme_name_nep) as name
      from pensioner_schemes
      where status = 1 and show_type=:showType
      order by scheme_code
      """)
  List<Tuple> getActivePensionerSchemeByShowType(@Param("showType") String showType);


  @Query(nativeQuery = true, value = "select * from pensioner_schemes where id = :id")
  Optional<PensionerSchemeResponseDto> getResponseById(@Param("id") long id);

  boolean existsBySchemeCode(String schemeCode);
}