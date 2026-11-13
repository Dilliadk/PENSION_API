package com.v1.pension.repository.pensioner;

import com.v1.pension.model.pensioner.PensionerBlockRelease;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PensionerBlockReleaseRepository extends JpaRepository<PensionerBlockRelease, Long> {
  @Query(nativeQuery = true, value = """
      select id, pension_subpno, organization, date_nep, remarks, stage,
      case type
      	when 1 then 'रोक्का'
      	when 2 then 'तामेली'
      	when 3 then 'फुकुवा'
      end as type
      from pensioner_block_release
      where pension_subpno = :subPNo
      order by date_nep desc
      """)
  List<Tuple> getAllForPensioner(@Param("subPNo") String subPNo);
}
