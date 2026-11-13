package com.v1.pension.repository.system;

import com.v1.pension.model.system.PensionerRelation;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PensionerRelationRepository extends JpaRepository<PensionerRelation, Long> {
  @Query(nativeQuery = true, value = """
      select id, relation_code as code, (relation_code || ' - ' || relation_name_nep) as name
      from pensioner_relation
      where status = 1
      order by relation_code
      """)
  List<Tuple> getActivePensionerRelationSummaries();


  @Query(nativeQuery = true, value = """
      select pr.id, relation_code, relation_name_nep, relation_name_eng, s.status_name_nep as status
      from pensioner_relation pr
      inner join status s on s.id = pr.status
      order by relation_code
      """)
  List<Tuple> getAllPensionerRelation();
}