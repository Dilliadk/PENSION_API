package com.v1.pension.repository.pensioner;

import java.util.List;

import jakarta.persistence.Tuple;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.v1.pension.model.pensioner.BankAccountCloseInformation;

public interface BankAccountCloseInformationRepository extends JpaRepository<BankAccountCloseInformation, Long> {
  @Query(nativeQuery = true, value = """
      select i.id, i.pension_subpno, ct.calculation_name_nep, i.voucher_master_id,
      case when p.pension_sub_pensioner = 0 then p.pension_name_nep
       else p.pension_name_nep || 'को ' || r.relation_name_nep || ' ' || p.pension_sub_name_nep
      end as pensioner_name,
      i.date
      from bank_account_close_information i
      inner join voucher_transaction_master vm on vm.id = i.voucher_master_id
      inner join pension_calculation_type ct on ct.calculation_code = vm.calculation_type_code
      inner join pensioner_registration p on p.pension_subpno = i.pension_subpno
      left join pensioner_relation r on r.relation_code = p.pension_relation
      where i.fiscal_year = :fy
        and i.status = 1 and
        (:officeCode = '00001' or i.office_code = :officeCode)
      order by date desc
      """)
  List<Tuple> getInformation(String fy, String officeCode);
}
