package com.v1.pension.repository.pensioner;

import com.v1.pension.model.pensioner.NidAndOtherDetail;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NidAndOtherDetailRepository extends JpaRepository<NidAndOtherDetail, Long> {
  @Query(nativeQuery = true, value = """
      select nod.id, nod.pension_subpno, nod.registration_date, nod.nid,
      nod.pan, nod.other, s.status_name_nep, nod.status, nod.stage
      from nid_and_other_details nod
      inner join status s on s.id = nod.status
      where nod.pension_subpno = :subPNo
      """)
  List<Tuple> getAllBySubPNo(@Param("subPNo") String subPNo);
}
