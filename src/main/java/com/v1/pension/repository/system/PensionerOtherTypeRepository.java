package com.v1.pension.repository.system;

import com.v1.pension.model.system.PensionerOtherType;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PensionerOtherTypeRepository extends JpaRepository<PensionerOtherType, Long> {
  @Query(nativeQuery = true, value = """
      select id, other_type_code as code, (other_type_code || ' - ' || other_type_name_nep) as name
      from pensioner_other_types
      where status = 1 order by 2 desc
      """)
  List<Tuple> getActivePensionerOtherTypeSummaries();


  @Query(nativeQuery = true, value = """
      select pot.id, other_type_code, other_type_name_nep,other_type_name_eng, s.status_name_nep as status
      from pensioner_other_types pot inner join status s on s.id = pot.status order by 2
      """)
  List<Tuple> getAllPensionerOtherType();
}