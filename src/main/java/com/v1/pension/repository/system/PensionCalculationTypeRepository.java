package com.v1.pension.repository.system;

import com.v1.pension.model.system.PensionCalculationType;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PensionCalculationTypeRepository extends JpaRepository<PensionCalculationType, Long> {
  @Query(nativeQuery = true, value = """
      select id, calculation_code as code, (calculation_code || ' - ' || calculation_name_nep) as name
      from pension_calculation_type
      where status = 1
      order by 2
      """)
  List<Tuple> getActivePensionCalculationTypeSummaries();

  @Query(nativeQuery = true, value = """
      select ct.id, calculation_code, calculation_name_nep,calculation_name_eng, s.status_name_nep as status
      from pension_calculation_type ct
      inner join status s on s.id = ct.status
      order by 2
      """)
  List<Tuple> getAllPensionCalculationType();
}