package com.v1.pension.repository.pensioner;

import java.util.List;

import jakarta.persistence.Tuple;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.v1.pension.dto.pensioner.presence.PensionerPresenceSummary;
import com.v1.pension.model.pensioner.PensionerPresence;

public interface PensionPresenceRepository extends JpaRepository<PensionerPresence, Long> {
  @Query(nativeQuery = true, value = """
      select pp.id, pp.pension_subpno, pp.present_date, pp.remarks,
      pp.nid, pp.file_data, s.status_name_nep, pp.status, pp.stage, pp.content_type
      from pensioner_presence pp
      inner join status s on s.id = pp.status
      where pp.pension_subpno = :subPNo
      --and pp.type = :type
      """)
  List<Tuple> getAllBySubPNo(String subPNo);

  @Query(nativeQuery = true, value = """
      select
      pp.present_date as presence_date_nep,
      pp.present_date_eng as presence_date_eng,
      case pp.type when 1 then 'Physical Presence' when 2 then 'Virtual Presence' when 3 then 'Manual Presence' end as presence_type,
      lr.nid_face_match_percentage
      from pensioner_presence pp
      left join liveness_results lr on lr.pensioner_presence_id = pp.id
      where pp.pension_subpno = :subPNo and pp.present_date_eng is not null and pp.status = 1
      order by pp.id desc
      """)
  List<PensionerPresenceSummary> getPresenceHistoryForPensioner(String subPNo);
}
