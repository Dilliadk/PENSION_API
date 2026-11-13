package com.v1.pension.repository.system;

import java.util.List;

import jakarta.persistence.Tuple;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.v1.pension.model.system.BankToPmoNotice;

public interface BankToPmoNoticeRepository extends JpaRepository<BankToPmoNotice, Long> {
  @Query(nativeQuery = true, value = """
        select n.id, n.pension_subpno, n.notice_date, o.office_name_nep as bank_branch_name, n.description,
        case when p.pension_sub_pensioner = 0 then p.pension_name_nep
          else p.pension_sub_name_nep
        end as pensioner_name
        from bank_to_pmo_notices n
        inner join offices o on o.office_code = n.bank_branch_code
        inner join pensioner_registration p on p.pension_subpno = n.pension_subpno
        where n.status = 1
        order by n.notice_date desc
      """)
  List<Tuple> getActiveNotices();
}
