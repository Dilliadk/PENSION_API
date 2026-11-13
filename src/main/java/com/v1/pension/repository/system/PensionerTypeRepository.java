package com.v1.pension.repository.system;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.Tuple;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.v1.pension.model.system.PensionerType;

public interface PensionerTypeRepository extends JpaRepository<PensionerType, Long> {
  Optional<PensionerType> findByTypeCode(String typeCode);

  @Query(nativeQuery = true, value = """
      select id, type_code as code,  ( type_code || ' - ' || type_name_nep) as name, is_Online
      from pensioner_types
      where status = 1 order by type_code
      """)
  List<Tuple> getActivePensionerTypeSummaries();

  @Query(nativeQuery = true, value = """
      select pt.id, type_code as code, type_name_nep as name, s.status_name_nep as status, is_online
      from pensioner_types pt
      inner join status s on s.id = pt.status
      order by type_code
      """)
  List<Tuple> getAllPensionerTypeSummaries();

  @Query(nativeQuery = true, value = """
      select id, type_code as code,  (type_code || ' - ' || type_name_nep) as name
      from pensioner_types
      where status = 1 and type_code in ('1', '2', '3', '4')
      order by type_code
      """)
  List<Tuple> getPayablePensionerTypes();
}