package com.v1.pension.repository.pensioner;

import com.v1.pension.model.pensioner.BankTransferLog;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BankTransferLogRepository extends JpaRepository<BankTransferLog, Long> {
  @Query(nativeQuery = true, value = """
      select tl.id, tl.created_by, tl.pension_subpno, tl.transfer_date,
      oot.description as old_main_bank, ob.office_name_nep as old_bank_branch,
      nt.description as new_main_bank, nb.office_name_nep as new_bank_branch
      from bank_transfer_logs tl
      inner join office_types oot on oot.id = tl.old_main_bank_code
      inner join offices ob on ob.office_code = tl.old_pension_bank_code
      inner join office_types nt on nt.id = tl.new_main_bank_code
      inner join offices nb on nb.office_code = tl.new_pension_bank_code
      where tl.pension_subpno = :subPNo
      order by tl.transfer_date desc
      """)
  List<Tuple> getBankTransferLogsForPensioner(String subPNo);
}
