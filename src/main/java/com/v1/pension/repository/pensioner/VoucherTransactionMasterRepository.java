package com.v1.pension.repository.pensioner;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.Tuple;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.v1.pension.model.pensioner.VoucherTransactionMaster;

public interface VoucherTransactionMasterRepository extends JpaRepository<VoucherTransactionMaster, Long> {
  @Query(nativeQuery = true, value = """
      SELECT (COALESCE(MAX(voucher_no::int8), 0) + 1)::text AS next_voucher_no
      FROM voucher_transaction_master
      WHERE voucher_no ~ '^[0-9]+$'
      """)
  String getNextVoucherNumber();

  @Query(nativeQuery = true, value = """
      select (:fy || '-' || (count(1) + 1)) as next_voucher_no
      from voucher_transaction_master
      where fiscal_year = :fy
      """)
  String getNextVoucherNumber(String fy);

  boolean existsByStartDateAndVoucherType(String startDate, int voucherType);

  @Query(nativeQuery = true, value = """
      select vm.id, vm.voucher_no, vm.voucher_type, vm.pension_subpno, vm.calculation_type_code, ct.calculation_name_nep,
      vm.calculation_from_date, vm.calculation_to_date, vm.status, vm.stage, s.stage_name_nep, vm.note, vm.forward_id,
      case when
        vm.voucher_type = 1
        and vm.stage = 4
        and vm.calculation_type_code in ('1', '2', '4', '5', '8', '9')
        and vm.office_code = '00001'
        and dsd.id is null
      then true
      else false end as needs_digital_signature,
      vm.office_code
      from voucher_transaction_master vm
      inner join pension_calculation_type ct on ct.calculation_code = vm.calculation_type_code
      inner join stages s on s.code = vm.stage
      left join digitally_signed_documents dsd
        on dsd.type = 'letter' and dsd.table_name = 'voucher_transaction_master' and dsd.record_id = vm.id
      where vm.voucher_type = 1 and vm.pension_subpno = :subPNo
      order by vm.id desc
        """)
  List<Tuple> getAdjustmentTransactionsForPensioner(String subPNo);

  @Query(nativeQuery = true, value = """
      select *
      from voucher_transaction_master
      where pension_subpno = :subPNo and voucher_type = 1 and stage = 4
      order by id desc
      limit 1
      """)
  Optional<VoucherTransactionMaster> findLatestApprovedAdjustmentVoucherForPensioner(String subPNo);

  @Query(nativeQuery = true, value = """
      select m.id, m.statement_date, m.start_date, m.end_date, m.status, m.stage, s.stage_name_nep,
      case m.voucher_type
        when 2 then 'Monthly Pension'
        when 3 then 'Increment Calculation'
      end as voucher_type_name,
      m.note, m.forward_id, string_to_array(m.user_ids, ',')::bigint[] as user_ids
      from voucher_transaction_master m
      inner join stages s on s.code = m.stage
      where m.voucher_type in (2,3)
      and m.fiscal_year = :fiscalYear
      and m.id > 142820925
      order by m.statement_date, m.start_date
      """)
  List<Tuple> getMonthlyAndIncrementVoucherTransactions(String fiscalYear);

  @Query(nativeQuery = true, value = """
      with details_data as (
        select vd.master_id, count(1) as total_pensioners,
        round(sum(coalesce(vd.total_amount, 0))::numeric, 2) * -1 as total_payable,
        round(sum(coalesce(vd.tax, 0))::numeric, 2) * -1 as total_tax
        from voucher_transaction_details vd
        inner join voucher_transaction_master vm on vm.id = vd.master_id
        where vd.master_id > 142820925 and vm.voucher_type = 4 and vm.fiscal_year = :fiscalYear
       		and vd.fiscal_year = :fiscalYear and vm.fiscal_year = :fiscalYear
        group by vd.master_id
      )
      select pm.id, pm.statement_date, pm.stage, pm.status, ot.name as main_bank_name,
      pt.type_name_nep, pm.note, pm.forward_id, s.stage_name_nep,
      coalesce(dd.total_pensioners, 0) as total_pensioners,
      coalesce(dd.total_payable, 0) as total_payable,
      coalesce(dd.total_tax, 0) as total_tax,
      case payment_type
        when 1 then 'Regular'
        when 2 then 'Tameli'
        when 3 then 'Mana-Chamal'
      end as payment_type,
      case pm.status
        when 1 then 'New Payment'
        when 2 then 'Sent For EFT'
        when 3 then 'EFT Success'
        when 4 then 'EFT Failure'
        when 5 then 'Revert'
      end as status_name
      from voucher_transaction_master pm
      inner join stages s on s.code = pm.stage
      inner join pensioner_types pt on pt.type_code = pm.pensioner_type_code
      left join office_types ot on ot.id = pm.main_bank_code
      left join details_data dd on dd.master_id = pm.id
      where pm.voucher_type = 4
        and pm.fiscal_year = :fiscalYear
        and pm.id > 142820925
        and (:status = -1 or pm.status = :status)
      order by pm.id desc
        """)
  List<Tuple> getPaymentVoucherTransactions(String fiscalYear, int status);

  @Query(nativeQuery = true, value = """
          select m.id, m.created_by, m.statement_date, m.start_date, m.end_date,
          s.stage_name_nep, m.stage, m.forward_id, nc.nepali_date as created_date,
          string_to_array(m.user_ids, ',')::bigint[] as user_ids,
          case m.voucher_type
            when 1 then 'Adjustment'
            when 2 then 'Monthly'
            when 3 then 'Increment'
            when 4 then 'Payment'
          end as voucher_type_name,
          m.voucher_type, m.voucher_no, m.pension_subpno, m.note as note, o.office_name_nep,
      m.calculation_type_code, pt.type_name_nep
          from voucher_transaction_master m
          inner join stages s on s.code = m.stage
          inner join nepali_calendar nc on nc.roman_date = m.created_date::date
          left join offices o on o.office_code = m.office_code
          left join pensioner_registration pr on pr.pension_subpno = m.pension_subpno and m.voucher_type = 1
          left join pensioner_types pt on pt.type_code = pr.pension_type_code
          left join digitally_signed_documents dsd
            on type = 'letter' and table_name = 'voucher_transaction_master' and record_id = m.id
          where
          (m.stage = 1 and :role = 'PMO_CREATOR') or
          (m.stage = 12 and :role = 'PMO_CREATOR') or --DTCO-Approved to PMO_CREATOR
          (m.stage = 3 and :role = 'PMO_APPROVER') or
          (m.stage = 9 and :role = 'DTCO_CREATOR' and m.office_code = :officeCode) or
          (m.stage = 11 and :role = 'DTCO_APPROVER' and m.office_code = :officeCode) or
          (:role = 'PMO_APPROVER'
              and m.id > 142826736
              and m.voucher_type = 1
              and m.stage = 4
              and m.calculation_type_code in ('1', '2', '4', '5', '8', '9')
              and m.office_code = '00001'
              and dsd.id is null
          )
          order by m.voucher_no
            """)
  List<Tuple> getApprovalPendingTransactions(String role, String officeCode);

  // @Query(nativeQuery = true, value = """
  //     select m.id, m.created_by, m.statement_date, m.start_date, m.end_date,
  //     s.stage_name_nep, m.stage, m.forward_id, nc.nepali_date as created_date,
  //     string_to_array(m.user_ids, ',')::bigint[] as user_ids,
  //     case m.voucher_type
  //       when 1 then 'Adjustment'
  //       when 2 then 'Monthly'
  //       when 3 then 'Increment'
  //       when 4 then 'Payment'
  //     end as voucher_type_name,
  //     m.voucher_type, m.voucher_no, m.pension_subpno, m.note as note, o.office_name_nep,
  //     m.calculation_type_code, pt.type_name_nep
  //     from voucher_transaction_master m
  //     inner join stages s on s.code = m.stage
  //     inner join nepali_calendar nc on nc.roman_date = m.created_date::date
  //     left join offices o on o.office_code = m.office_code
  //     left join pensioner_registration pr on pr.pension_subpno = m.pension_subpno and m.voucher_type = 1
  //     left join pensioner_types pt on pt.type_code = pr.pension_type_code
  //     where
  //     (m.stage = 1 and :role = 'PMO_CREATOR') or
  //     (m.stage = 12 and :role = 'PMO_CREATOR') or --DTCO-Approved to PMO_CREATOR
  //     (m.stage = 3 and :role = 'PMO_APPROVER') or
  //     (m.stage = 9 and :role = 'DTCO_CREATOR' and m.office_code = :officeCode) or
  //     (m.stage = 11 and :role = 'DTCO_APPROVER' and m.office_code = :officeCode)
  //     order by m.voucher_no
  //       """)
  // List<Tuple> getApprovalPendingTransactions(String role, String officeCode);

  @Query(nativeQuery = true, value = "select user_ids from voucher_transaction_master where id = :id")
  String getUserIds(long id);

  @Modifying
  @Query(nativeQuery = true, value = """
      update voucher_transaction_master
      set stage = :stage, note=:note, forward_id=:forwardId
      where id = :id
      """)
  void updateForward(long id, Integer stage, long forwardId, String note);

  @Modifying
  @Query(nativeQuery = true, value = "REFRESH MATERIALIZED VIEW CONCURRENTLY mv_dashboard_payments")
  void updateDashboardPaymentView();

  @Modifying
  @Query(nativeQuery = true, value = """
        REFRESH MATERIALIZED VIEW CONCURRENTLY mv_pensioner_balance
      """)
  void updatePensionerBalanceView();

  @Query(nativeQuery = true, value = """
      select id
      from voucher_transaction_master
      where pension_subpno = :subPNo
      and case when office_code = '00001' then stage =  4
        else stage in (3,4,12) end
      order by created_date desc
      limit 1
      """)
  Optional<Long> getLatestAdjustmentVoucherIdForPensioner(String subPNo);

  @Query(nativeQuery = true, value = """
      select coalesce(
      (select max(id) from voucher_transaction_master
      where pension_subpno = :subPNo and stage != 100 and voucher_type = 1),
      0)::int8
      """)
  long getLastAdjustmentVoucherIdForPensioner(String subPNo);

  @Query(nativeQuery = true, value = """
      select coalesce((select additional_pay_amount from voucher_transaction_master where id = :id), 0)
      """)
  double getLastAdjustmentVoucherAmountForId(long id);

  @Query(nativeQuery = true, value = """
      select coalesce(max(vm.id), 0)::int8 from voucher_transaction_details vd
      inner join voucher_transaction_master vm on vm.id = vd.master_id
      where vd.pension_subpno = :subPNo and vm.stage != 100 and vm.voucher_type = 4
      """)
  long getLastPaymentVoucherIdForPensioner(String subPNo);

  @Query(nativeQuery = true, value = """
      select exists (
        select 1 from voucher_transaction_master
        where id > 142820925
        and voucher_type = 4
        and stage != 4
        and payment_type = :paymentType
        and pensioner_type_code = :typeCode
        and coalesce(main_bank_code, 0) = :mainBankCode
      )
      """)
  boolean existsPendingPayment(int paymentType, String typeCode, int mainBankCode);
}
