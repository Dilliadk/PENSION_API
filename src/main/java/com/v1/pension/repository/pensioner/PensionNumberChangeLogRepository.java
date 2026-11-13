package com.v1.pension.repository.pensioner;

import java.util.List;

import jakarta.persistence.Tuple;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.v1.pension.model.pensioner.PensionNumberChangeLog;

public interface PensionNumberChangeLogRepository extends JpaRepository<PensionNumberChangeLog, Long> {
  @Query(nativeQuery = true, value = """
      select cl.id, cl.created_by,
      nc.nepali_date || ' ' || to_char(cl.created_date, 'HH:MI:SS AM') as created_at,
      cl.old_value, cl.new_value
      from pension_number_change_logs cl
      inner join nepali_calendar nc on nc.roman_date = cl.created_date::date
      order by id desc
      """)
  List<Tuple> getPensionNumberChangeLogs();

  @Modifying
  @Query(nativeQuery = true, value = """
      update pensioner_registration
      set pension_subpno = :newPn,
      pension_no = case when pension_no = pension_subpno then :newPn else pension_no end
      where pension_subpno = :oldPn;

      update bank_account_close_information set pension_subpno = :newPn where pension_subpno = :oldPn;

      update bank_accounts set pension_subpno = :newPn where pension_subpno = :oldPn;

      update bank_to_pmo_notices set pension_subpno = :newPn where pension_subpno = :oldPn;

      update bank_transfer_logs set pension_subpno = :newPn where pension_subpno = :oldPn;

      update liveness_results set pension_subpno = :newPn where pension_subpno = :oldPn;

      update nid_and_other_details set pension_subpno = :newPn where pension_subpno = :oldPn;

      update pensioner_block_release set pension_subpno = :newPn where pension_subpno = :oldPn;

      update pensioner_documents set pension_subpno = :newPn where pension_subpno = :oldPn;

      update pensioner_presence set pension_subpno = :newPn where pension_subpno = :oldPn;

      update voucher_transaction_master set pension_subpno = :newPn where pension_subpno = :oldPn;

      update voucher_transaction_details set pension_subpno = :newPn where pension_subpno = :oldPn;
      """)
  void changePensionNumber(String oldPn, String newPn);
}
