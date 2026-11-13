package com.v1.pension.repository.pensioner;

import java.util.List;

import jakarta.persistence.Tuple;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.v1.pension.dto.ipfms.IpfmsPaymentDetail;
import com.v1.pension.dto.pensioner.voucher_transaction.PayeeCodeInfo;
import com.v1.pension.dto.pensioner.voucher_transaction.PaymentEligibleInfoDto;
import com.v1.pension.dto.pensioner.voucher_transaction.TameliPaymentInfo;
import com.v1.pension.model.pensioner.VoucherTransactionDetail;

public interface VoucherTransactionDetailRepository extends JpaRepository<VoucherTransactionDetail, Long> {
  @Query(nativeQuery = true, value = "select * from voucher_transaction_details where master_id = :masterId")
  List<VoucherTransactionDetail> getDetailsByMaster(long masterId);

  @Query(nativeQuery = true, value = """
      select vd.id, pr.id::varchar as payee_code
      from voucher_transaction_details vd
      inner join pensioner_registration pr on pr.pension_subpno = vd.pension_subpno
      where vd.master_id = :masterId
      """)
  List<PayeeCodeInfo> getPayeeCodes(long masterId);

  @Query(nativeQuery = true, value = """
        select md.id, md.master_id, pp.post_name_nep,
        md.receivable_amount, md.increment, md.tax, md.total_amount,
        case when p.pension_sub_pensioner = 0 then p.pension_name_nep
          else p.pension_name_nep || 'को ' || r.relation_name_nep || ' ' || p.pension_sub_name_nep
        end as name_nep,
        case when p.pension_sub_pensioner = 0 then p.pension_name_eng
            else p.pension_name_eng || '''s ' || r.relation_name_eng || ' ' || p.pension_sub_name_eng
        end as name_eng
        from voucher_transaction_details md
        inner join pensioner_registration p on p.pension_subpno = md.pension_subpno
        left join pensioner_posts pp on pp.pensioner_post_code = p.pension_post_code
        left join pensioner_relation r on r.relation_code = p.pension_relation
        where md.master_id = :masterId
      """)
  List<Tuple> getMonthlyVoucherDetailsByMaster(long masterId);

  @Query(nativeQuery = true, value = """
      select
      (select coalesce(sum(total_payment_amt), 0) from voucher_transaction_master
        where voucher_type = 1 and stage = 4 and status = 1 and pension_subpno = :subPNo and id != :masterId
      )
      +
      (select coalesce(sum(vd.receivable_amount), 0)
      from voucher_transaction_details vd
      inner join voucher_transaction_master vm on vm.id = vd.master_id
      where vm.voucher_type in (2,4) and vm.stage = 4 and vd.status = 1
      and vd.pension_subpno = :subPNo
      and vd.fiscal_year = :fiscalYear
      and vm.id != :masterId)
      """)
  double getTotalReceivableForFy(String subPNo, String fiscalYear, Long masterId);

  // @Query(nativeQuery = true, value = """
  //     select
  //     (select coalesce(sum(tax), 0) from voucher_transaction_master
  //     where voucher_type = 1 and stage = 4 and status = 1 and pension_subpno = :subPNo and id != :masterId)
  //     +
  //     (select coalesce(sum(vd.tax), 0)
  //     from voucher_transaction_details vd
  //     inner join voucher_transaction_master vm on vm.id = vd.master_id
  //     --where vm.voucher_type in (2,4) and vd.status = 1
  //     where vm.voucher_type in (2, 4) and stage = 4 and vd.status in (1, 2, 3)
  //     and vd.pension_subpno = :subPNo
  //     and vd.fiscal_year = :fiscalYear
  //     and vm.id != :masterId)
  //     """)
  // double getTotalPaidTaxForFy(String subPNo, String fiscalYear, long masterId);

  @Query(nativeQuery = true, value = """
      select coalesce(sum(abs(vd.tax)), 0) as tax_paid
      from voucher_transaction_details vd
      inner join voucher_transaction_master vm on vm.id = vd.master_id
      where vm.voucher_type = 4
      and vm.fiscal_year = :fy
      and vm.stage = 4
      and vd.pension_subpno = :subPNo
      and vd.status in (2,3)
        """)
  double getTotalPaidTaxForFy(String subPNo, String fy);

  @Query(nativeQuery = true, value = """
      select coalesce(sum(abs(vd.total_amount)), 0) + coalesce(sum(abs(vd.tax)), 0) as amount_paid
      from voucher_transaction_details vd
      inner join voucher_transaction_master vm on vm.id = vd.master_id
      where vm.voucher_type = 4
      and vm.fiscal_year = :fy
      and vm.stage = 4
      and vd.pension_subpno = :subPNo
      and vd.status in (2,3)
        """)
  double getTotalPaidAmountForFy(String subPNo, String fy);

  @Query(nativeQuery = true, value = """
        select md.id, md.master_id, pp.post_name_nep,
        abs(md.receivable_amount) as "receivableAmount", md.increment,
        abs(md.tax) as tax, abs(md.total_amount) as "totalAmount",
        case when p.pension_sub_pensioner = 0 then p.pension_name_nep
          else p.pension_name_nep || 'को ' || r.relation_name_nep || ' ' || p.pension_sub_name_nep
        end as name_nep,
        case when p.pension_sub_pensioner = 0 then p.pension_name_eng
          else p.pension_name_eng || '''s ' || r.relation_name_eng || ' ' || p.pension_sub_name_eng
        end as name_eng
        from voucher_transaction_details md
        inner join pensioner_registration p on p.pension_subpno = md.pension_subpno
        left join pensioner_posts pp on pp.pensioner_post_code = p.pension_post_code
        left join pensioner_relation r on r.relation_code = p.pension_relation
        where md.master_id = :masterId
      """)
  List<Tuple> getPaymentVoucherDetailsByMaster(long masterId);

  @Query(nativeQuery = true, value = """
      select vd.id as "pensionPaymentDetailId", '' as pan, vd.payee_code as "payeeCode",
      vd.account_name_eng as ename,
      vd.account_name_nep as nname,
      vd.total_amount as "payableAmount",
      0 as "socialTaxAmount",
      vd.tax as "tdsAmount",
      vd.receivable_amount as "totalAmount",
      vd.bank_id as "bankId",
      vd.account_no as "bankAccountNo"
      from voucher_transaction_details vd
      where vd.master_id = :masterId and vd.status = 1
      and vd.account_name_eng is not null and length(vd.account_name_eng) > 4
      and vd.account_name_nep is not null and length(vd.account_name_nep) > 4
      """)
  List<IpfmsPaymentDetail> getRegularPaymentDetailsForIpfms(long masterId);

  @Query(nativeQuery = true, value = """
      select vd.id as "pensionPaymentDetailId", '' as pan, vd.payee_code as "payeeCode",
      vd.account_name_eng as ename,
      vd.account_name_nep as nname,
      vd.total_amount as "payableAmount",
      0 as "socialTaxAmount",
      vd.tax as "tdsAmount",
      vd.receivable_amount as "totalAmount",
      vd.bank_id as "bankId",
      vd.account_no as "bankAccountNo"
      from voucher_transaction_details vd
      where vd.master_id = :masterId and vd.status = 1
      and vd.account_name_eng is not null and length(vd.account_name_eng) > 4
      and vd.bank_id is not null
      and vd.account_no is not null
      """)
  List<IpfmsPaymentDetail> getTameliPaymentDetailsForIpfms(long masterId);

  @Query(nativeQuery = true, value = """
      select vd.id as "pensionPaymentDetailId", '' as pan, vd.payee_code as "payeeCode",
      vd.account_name_eng as ename,
      vd.account_name_nep as nname,
      vd.total_amount as "payableAmount",
      0 as "socialTaxAmount",
      vd.tax as "tdsAmount",
      vd.receivable_amount as "totalAmount",
      vd.bank_id as "bankId",
      vd.account_no as "bankAccountNo"
      from voucher_transaction_details vd
      where vd.master_id = :masterId and vd.status = 1
        and vd.account_name_eng is not null and length(vd.account_name_eng) > 4
        and vd.bank_id is not null
        and vd.account_no is not null
      """)
  List<IpfmsPaymentDetail> getManachamalPaymentDetailsForIpfms(long masterId);

  @Modifying
  @Query(nativeQuery = true, value = """
      update voucher_transaction_details
      set status = :status
      where id in (:detailIds)
      """)
  void updateDetailsStatus(List<Long> detailIds, int status);

  @Modifying
  @Query(nativeQuery = true, value = """
      delete from voucher_transaction_details
      where master_id = :masterId and status = 1
      """)
  void deleteEftIneligibleDetails(long masterId);

  @Query(nativeQuery = true, value = """
        with block_release_data as (
            select pbr.pension_subpno,
            count(case when pbr.type = 3 then 1 end) - count(case when pbr.type in (1,2) then 1 end) as release_to_block_difference
            from pensioner_block_release pbr
            group by pbr.pension_subpno
        ),
        pensioner_info as (
            select p.pension_subpno, p.pension_status, p.pension_end_date_nep, p.national_id,
            p.pension_sub_pensioner, p.pension_account_no, p.pension_amount_per_month,
            p.pension_gender, p.other_type_code, p.pension_marital_status, p.festival_month,
            p.pension_name_eng, p.pension_sub_name_eng
            from pensioner_registration p
            where p.main_bank_code = :mainBankCode and p.pension_type_code = :pensionTypeCode
        ),
        voucher_info as (
          select * from (
            select row_number() over(partition by pension_subpno order by id desc) as rn, id,
            pension_subpno, new_pension_amount, new_pension_end_date, calculation_from_date, calculation_to_date,
            case when fiscal_year = :fy and legal_case = true then true else false end as legal_case
            from voucher_transaction_master
              where voucher_type = 1 and stage = 4 and id > 142820925
          ) where rn = 1
        ),
        monthly_info as (
          select * from (
            select calculation_from_date as last_monthly_pension_date
            from voucher_transaction_master
            where voucher_type = 2 and stage = 4 and id > 142820925
            order by id desc limit 1
          )
        )
        select
          pi.pension_subpno as sub_number, coalesce(b.balance, 0) as balance, vi.calculation_from_date, vi.calculation_to_date,
          coalesce(vi.legal_case, false) as legal_case,
          pi.pension_gender, pi.other_type_code, pi.pension_marital_status, pi.pension_end_date_nep, pi.festival_month,
          coalesce(vi.new_pension_amount, pi.pension_amount_per_month) as monthly_amount,
          case when coalesce(vi.calculation_to_date, '') > coalesce(mi.last_monthly_pension_date, '')
            then coalesce(vi.calculation_to_date, '') else coalesce(mi.last_monthly_pension_date, '')
          end as last_monthly_pension_date
          from pensioner_info pi
          cross join monthly_info mi
          left join voucher_info vi on vi.pension_subpno = pi.pension_subpno
          inner join mv_pensioner_balance b on b.pension_subpno = pi.pension_subpno
          left join block_release_data brd on brd.pension_subpno = pi.pension_subpno
          where
            (brd.release_to_block_difference is null or brd.release_to_block_difference >= 0)
            and (
              case when vi.id is not null then vi.new_pension_end_date else pi.pension_end_date_nep end is null
              or case when vi.id is not null then vi.new_pension_end_date else pi.pension_end_date_nep end = ''
              or case when vi.id is not null then vi.new_pension_end_date else pi.pension_end_date_nep end >= :todayDateNep
            )
            and (coalesce(pi.pension_status, 0) = 1)
            and (pi.national_id is not null and pi.national_id != '')
            and (coalesce(length(pi.pension_account_no), 0) in (16, 20) and
              length((case when pi.pension_sub_pensioner = 0 then coalesce(pi.pension_name_eng, '')
                else coalesce(pi.pension_sub_name_eng, '') end)
              ) >= 4
            )
            and b.balance >= 1
            and pi.pension_subpno not in (select pension_subpno from payment_excluded_pensioners)
            and
              ((select coalesce((current_date - max(present_date_eng)), -1) from pensioner_presence
                where pension_subpno = pi.pension_subpno
              ) between 0 and case when pi.pension_sub_pensioner = 0 then 365 else 183 end)
      """)
  List<PaymentEligibleInfoDto> getRegularPayablePensionersInfo(int mainBankCode, String todayDateNep,
      String pensionTypeCode, String fy);

  @Query(nativeQuery = true, value = """
        with block_release_data as (
            select pbr.pension_subpno,
            count(case when pbr.type = 3 then 1 end) - count(case when pbr.type in (1,2) then 1 end) as release_to_block_difference
            from pensioner_block_release pbr
            group by pbr.pension_subpno
        ),
        pensioner_info as (
            select p.pension_subpno, p.pension_status, p.pension_end_date_nep, p.national_id,
            p.pension_sub_pensioner, p.pension_account_no, p.pension_amount_per_month,
            p.pension_gender, p.other_type_code, p.pension_marital_status, p.festival_month,
            p.pension_name_eng, p.pension_sub_name_eng
            from pensioner_registration p
            where p.pension_type_code in (:pensionTypes)
        ),
        voucher_info as (
          select * from (
            select row_number() over(partition by pension_subpno order by id desc) as rn, id,
            pension_subpno, new_pension_amount, new_pension_end_date, calculation_from_date, calculation_to_date,
            case when fiscal_year = :fy and legal_case = true then true else false end as legal_case
            from voucher_transaction_master
              where voucher_type = 1 and stage = 4 and id > 142820925
          ) where rn = 1
        ),
        monthly_info as (
          select * from (
            select calculation_from_date as last_monthly_pension_date
            from voucher_transaction_master
            where voucher_type = 2 and stage = 4 and id > 142820925
            order by id desc limit 1
          )
        )
        select
          pi.pension_subpno as sub_number, coalesce(b.balance, 0) as balance, vi.calculation_from_date, vi.calculation_to_date,
          coalesce(vi.legal_case, false) as legal_case,
          pi.pension_gender, pi.other_type_code, pi.pension_marital_status, pi.pension_end_date_nep, pi.festival_month,
          coalesce(vi.new_pension_amount, pi.pension_amount_per_month) as monthly_amount,
          case when coalesce(vi.calculation_to_date, '') > coalesce(mi.last_monthly_pension_date, '')
            then coalesce(vi.calculation_to_date, '') else coalesce(mi.last_monthly_pension_date, '')
          end as last_monthly_pension_date
          from pensioner_info pi
          cross join monthly_info mi
          left join voucher_info vi on vi.pension_subpno = pi.pension_subpno
          inner join mv_pensioner_balance b on b.pension_subpno = pi.pension_subpno
          left join block_release_data brd on brd.pension_subpno = pi.pension_subpno
          where
            (brd.release_to_block_difference is null or brd.release_to_block_difference >= 0)
            and (
              case when vi.id is not null then vi.new_pension_end_date else pi.pension_end_date_nep end is null
              or case when vi.id is not null then vi.new_pension_end_date else pi.pension_end_date_nep end = ''
              or case when vi.id is not null then vi.new_pension_end_date else pi.pension_end_date_nep end >= :todayDateNep
            )
            and (coalesce(pi.pension_status, 0) = 1)
            and (pi.national_id is not null and pi.national_id != '')
            and (coalesce(length(pi.pension_account_no), 0) in (16, 20) and
              length((case when pi.pension_sub_pensioner = 0 then coalesce(pi.pension_name_eng, '')
                else coalesce(pi.pension_sub_name_eng, '') end)
              ) >= 4
            )
            and b.balance >= 1
            and pi.pension_subpno not in (select pension_subpno from payment_excluded_pensioners)
            and
              ((select coalesce((current_date - max(present_date_eng)), -1) from pensioner_presence
                where pension_subpno = pi.pension_subpno
              ) between 0 and case when pi.pension_sub_pensioner = 0 then 365 else 183 end)
      """)
  List<PaymentEligibleInfoDto> getDailyPayablePensionersInfo(String todayDateNep,
      List<String> pensionTypes, String fy);

  @Query(nativeQuery = true, value = """
      with voucher_info as (
        select * from (
          select row_number() over(partition by pension_subpno order by id desc) as rn, pension_subpno,
          case when fiscal_year = :fy and legal_case = true then true else false end as legal_case
          from voucher_transaction_master
            where voucher_type = 1 and stage = 4 and id > 142820925
        ) where rn = 1
      )
      select distinct vm.pension_subpno as pension_number, b.balance,
      p.pension_gender, p.other_type_code, p.pension_marital_status,
      coalesce(vi.legal_case, false) as legal_case
      from voucher_transaction_master vm
      inner join pensioner_registration p on p.pension_subpno = vm.pension_subpno
      inner join mv_pensioner_balance b on b.pension_subpno = p.pension_subpno
      left join voucher_info vi on vi.pension_subpno = vm.pension_subpno
      where vm.voucher_type = 1
      and vm.stage = 4
      and vm.calculation_type_code in ('8', '9')
      and vm.nominee_bank_code is not null
      and vm.nominee_bank_account_no is not null
      and vm.nominee is not null
      and b.balance >= 1
      and p.pension_type_code in (:pensionerTypes)
        """)
  List<TameliPaymentInfo> getTameliPaymentInfo(List<String> pensionerTypes, String fy);

  @Query(nativeQuery = true, value = """
      select distinct vm.pension_subpno as pension_number, b.balance, p.pension_gender, p.other_type_code,
      p.pension_marital_status, true as legal_case,
      vm.nominee, vm.nominee_bank_code::int4 as nominee_bank_code,
      vm.nominee_bank_account_no, p.id::varchar as payee_code
      from voucher_transaction_master vm
      inner join pensioner_registration p on p.pension_subpno = vm.pension_subpno
      inner join mv_pensioner_balance b on b.pension_subpno = p.pension_subpno
      where vm.voucher_type = 1
        and vm.stage = 4
        and vm.new_scheme_code = '12'
        and vm.nominee_bank_code is not null and vm.nominee_bank_code <> ''
        and vm.nominee_bank_account_no is not null
        and vm.nominee is not null
        and b.balance >= 1
        and p.pension_type_code in (:pensionerTypes)
      """)
  List<TameliPaymentInfo> getManachamalPaymentInfo(List<String> pensionerTypes);

  @Query(nativeQuery = true, value = "select * from voucher_transaction_details where eft_no in (:eftNumbers)")
  List<VoucherTransactionDetail> getTransactionDetailsByEftNumbers(List<String> eftNumbers);
}
