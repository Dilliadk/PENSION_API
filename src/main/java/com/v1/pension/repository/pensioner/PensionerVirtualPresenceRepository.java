package com.v1.pension.repository.pensioner;

import com.v1.pension.model.pensioner.PensionerVirtualPresence;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PensionerVirtualPresenceRepository extends JpaRepository<PensionerVirtualPresence, Long> {
  @Query(nativeQuery = true, value = """
      select pvp.id, pvp.pension_subpno, pvp.registration_date,
      s.status_name_nep, pvp.status, pvp.stage
      from pensioner_virtual_presence pvp
      inner join status s on s.id = pvp.status
      where pvp.pension_subpno = :subPNo
      """)
  List<Tuple> getAllBySubPNo(@Param("subPNo") String subPNo);

  @Query(nativeQuery = true, value = """
      select * from pensioner_virtual_presence
      where pension_subpno = :subPNo and status = 1
      order by created_date desc
      limit 1
      """)
  Optional<PensionerVirtualPresence> findLatestBySubPNo(@Param("subPNo") String subPNo);
}
