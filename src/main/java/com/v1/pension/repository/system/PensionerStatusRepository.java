package com.v1.pension.repository.system;

import com.v1.pension.model.system.PensionerStatus;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PensionerStatusRepository extends JpaRepository<PensionerStatus, Long> {
  @Query(nativeQuery = true, value = """
      select id, status_code as code, (status_code || ' - ' || status_name_nep) as name
      from pensioner_status
      where status = 1 order by 2
      """)
  List<Tuple> getActivePensionerStatusSummaries();


  @Query(nativeQuery = true, value = """
      select ps.id, status_code, ps.status_name_nep, ps.status_name_eng, s.status_name_nep as status
      from pensioner_status ps inner join status s on s.id = ps.status order by 2
      """)
  List<Tuple> getAllPensionerStatus();
}