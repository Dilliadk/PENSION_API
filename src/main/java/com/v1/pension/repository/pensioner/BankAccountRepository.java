package com.v1.pension.repository.pensioner;

import java.util.List;

import jakarta.persistence.Tuple;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.v1.pension.model.pensioner.BankAccount;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
  @Query(nativeQuery = true, value = """
      select count(1) from bank_accounts
      where pension_subpno = :subPNo and status = 1 and account_no != :acNo
      """)
  int getExtraActiveBankAccountCountForPensioner(String subPNo, String acNo);

  @Query(nativeQuery = true, value = "select count(1) from bank_accounts where pension_subpno = :subPNo")
  int getBankAccountCountForPensioner(String subPNo);

  @Query(nativeQuery = true, value = """
      select ba.id, ba.pension_subpno, ba.registration_date,
      ba.account_name, ba.account_no, s.status_name_nep, ba.status, ba.stage
      from bank_accounts ba
      inner join status s on s.id = ba.status
      where ba.pension_subpno = :subPNo
      order by ba.registration_date desc
      """)
  List<Tuple> getAllBySubPNo(@Param("subPNo") String subPNo);

  @Modifying
  @Query(nativeQuery = true, value = """
      update bank_accounts set status = 2
      where pension_subpno = :subPNo and status = 1
      """)
  void inactivateAccountsForPensioner(String subPNo);
}
