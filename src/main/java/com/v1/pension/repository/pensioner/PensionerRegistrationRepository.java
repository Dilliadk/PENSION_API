package com.v1.pension.repository.pensioner;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.Tuple;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.v1.pension.dto.tp_integration.PensionerInfoForNagarikApp;
import com.v1.pension.model.pensioner.PensionerRegistration;

public interface PensionerRegistrationRepository extends JpaRepository<PensionerRegistration, Long> {
  // @Query(nativeQuery = true, value = """
  //     with voucher_info as (
  //             select * from (
  //               select row_number() over(partition by pension_subpno order by id desc) as rn, id,
  //               pension_subpno, new_pension_start_date, new_pension_end_date, new_pension_amount, legal_case,
  //               calculation_from_date, calculation_to_date
  //               from voucher_transaction_master
  //               where voucher_type = 1 and stage = 4 and id > 142812237
  //             ) where rn = 1
  //           ),
  //           pending_vouchers as (
  //             select * from (
  //               select row_number() over(partition by pension_subpno order by id desc) as rn, pension_subpno
  //               from voucher_transaction_master
  //               where voucher_type = 1 and stage not in (4, 100) and id > 142812237
  //             ) where rn = 1
  //           ),
  //           amount_info as (
  //             select t1.pension_subpno,
  //             t1.amount1 + t2.amount2 as total_receivable
  //             --t1.tax1 + t3.tax2 as total_paid_tax
  //             from
  //                 (select pension_subpno,
  //                   coalesce(sum(total_payment_amt), 0) as amount1,
  //                   coalesce(sum(tax), 0) as tax1
  //                   from voucher_transaction_master
  //                   where voucher_type = 1 and status = 1 and stage = 4
  //                   group by pension_subpno
  //                 ) t1
  //                 inner join
  //                 (select vd.pension_subpno, coalesce(sum(vd.receivable_amount), 0) as amount2
  //                 from voucher_transaction_details vd
  //                 inner join voucher_transaction_master vm on vm.id = vd.master_id
  //                 where vm.voucher_type in (2,4) and vm.stage = 4 and vd.status = 1 and vd.fiscal_year = :fiscalYear
  //                 group by vd.pension_subpno
  //                 ) t2 on t2.pension_subpno = t1.pension_subpno
  //                  -- tax from payment
  //                 --inner join
  //                 --(select vd.pension_subpno, coalesce(sum(vd.tax), 0) as tax2
  //                 --from voucher_transaction_details vd
  //                 --inner join voucher_transaction_master vm on vm.id = vd.master_id
  //                 --where vd.fiscal_year = :fiscalYear and vd.status in (1, 2, 3) and vm.voucher_type in (2, 4) and vm.stage = 4
  //                 --group by vd.pension_subpno
  //                 --) t3 on t3.pension_subpno = t1.pension_subpno
  //           )
  //           select
  //           pr.pension_subpno as sub_number,
  //           pr.festival_month,
  //           pr.pension_amount_per_month,
  //           vi.new_pension_amount as pension_amount_per_month_from_voucher,
  //           coalesce(vi.legal_case, false) as legal_case,
  //           coalesce(ai.total_receivable, 0) as total_receivable,
  //           pr.other_type_code,
  //           pr.pension_marital_status,
  //           pr.pension_gender,
  //           case when vi.id is not null then vi.new_pension_end_date else pr.pension_end_date_nep end as pension_end_date_nep,
  //           vi.calculation_from_date,
  //           vi.calculation_to_date
  //           from pensioner_registration pr
  //           left join voucher_info vi on vi.pension_subpno = pr.pension_subpno
  //           left join amount_info ai on ai.pension_subpno = pr.pension_subpno
  //           where pr.pension_status in (1,2,3)
  //           and pr.pension_subpno not in (select pension_subpno from pending_vouchers)
  //           and coalesce(vi.new_pension_start_date, pr.pension_date_nep) is not null
  //           and coalesce(vi.new_pension_start_date, pr.pension_date_nep) <> ''
  //           and coalesce(vi.new_pension_start_date, pr.pension_date_nep) <= :calculationFromDate
  //           """)
  // List<PensionerInfoForMonthlyGeneration> getPensionersForMonthlyGeneration(String fiscalYear,
  //     String calculationFromDate);

  @Query(nativeQuery = true, value = """
      with voucher_info as (
        select * from (
          select row_number() over(partition by vm.pension_subpno order by vm.id desc) as rn, vm.id,
          vm.pension_subpno, vm.new_pension_start_date, vm.new_pension_end_date, vm.new_pension_amount, vm.legal_case,
          vm.calculation_from_date, vm.calculation_to_date, vm.new_scheme_code, nc.nepali_date as creation_date
          from voucher_transaction_master vm
          inner join nepali_calendar nc on nc.roman_date = vm.created_date::date
          where vm.voucher_type = 1 and vm.stage = 4 and vm.id > 142812237
        ) where rn = 1
      ),
      pending_vouchers as (
        select * from (
          select row_number() over(partition by pension_subpno order by id desc) as rn, pension_subpno
          from voucher_transaction_master
          where voucher_type = 1 and stage not in (4, 100) and id > 142812237
        ) where rn = 1
      ),
      amount_info as (
        select t1.pension_subpno,
        t1.amount1 + t2.amount2 as total_receivable
        from
            (select pension_subpno,
              coalesce(sum(total_payment_amt), 0) as amount1,
              coalesce(sum(tax), 0) as tax1
              from voucher_transaction_master
              where voucher_type = 1 and status = 1 and stage = 4
              group by pension_subpno
            ) t1
            inner join
            (select vd.pension_subpno, coalesce(sum(vd.receivable_amount), 0) as amount2
              from voucher_transaction_details vd
              inner join voucher_transaction_master vm on vm.id = vd.master_id
              where vm.voucher_type in (2,4) and vm.stage = 4 and vd.status = 1 and vd.fiscal_year = :fiscalYear
              group by vd.pension_subpno
            ) t2 on t2.pension_subpno = t1.pension_subpno
      ),
      increment_info as (
        select scheme_code, post_code, sum(increment_amount) as increment_amount
        from pension_increments
        where id >= 23274 and status = 1
        group by scheme_code, post_code
      )
      select
      vi.id as voucher_master_id,
      pr.pension_subpno as sub_number,
      pr.festival_month,
      case when vi.creation_date is not null
        and vi.creation_date between '2083-04-01' and '2083-04-25'
        and :calculationFromDate between '2083-04-01' and '2083-04-31'
          then 0
          else coalesce(vi.new_pension_amount, pr.pension_amount_per_month)
      end as monthly_amount,
      case when (vi.creation_date is not null and vi.creation_date > '2083-04-25') or (pr.pension_retire_nep > '2083-04-01')
        then 0
        else coalesce(ii.increment_amount, 0) * (pr.pension_increment_percentage/100)
      end	as increment_amount, -- on every future increment, set these dates accordingly
      coalesce(ai.total_receivable, 0) as total_receivable,
      case when vi.id is not null then vi.new_pension_end_date else pr.pension_end_date_nep end as pension_end_date_nep,
      vi.calculation_from_date,
      vi.calculation_to_date
      from pensioner_registration pr
      left join voucher_info vi on vi.pension_subpno = pr.pension_subpno
      left join amount_info ai on ai.pension_subpno = pr.pension_subpno
      left join increment_info ii
        on ii.scheme_code = coalesce(vi.new_scheme_code, pr.pension_scheme_code) and ii.post_code = pr.pension_post_code
      where pr.pension_status in (1,2,3)
      and pr.pension_subpno not in (select pension_subpno from pending_vouchers)
      and coalesce(vi.new_pension_start_date, pr.pension_date_nep) is not null
      and coalesce(vi.new_pension_start_date, pr.pension_date_nep) <> ''
      and coalesce(vi.new_pension_start_date, pr.pension_date_nep) <= :calculationFromDate
      """)
  List<Tuple> getMonthlyPensionInformation(String fiscalYear, String calculationFromDate);

  @Modifying
  @Query(nativeQuery = true, value = "REFRESH MATERIALIZED VIEW CONCURRENTLY monthly_voucher_eligible_pensioners")
  void refreshMonthlyVoucherEligiblePensioners();

  @Query(nativeQuery = true, value = "select updated_at from monthly_voucher_eligible_pensioners limit 1")
  LocalDateTime getLastRefreshedTime();

  boolean existsByPensionNo(String pensionNo);

  boolean existsByPensionSubPNo(String pensionSubPNo);

  Optional<PensionerRegistration> findByPensionSubPNo(String pensionSubPNo);

  @Query(nativeQuery = true, value = "select national_id from pensioner_registration where pension_subpno = :pensionSubPNo")
  String getNationalIdForPensioner(String pensionSubPNo);

  @Query(nativeQuery = true, value = """
      select p.id, p.pension_subpno,
      case when p.pension_sub_pensioner = 0 then p.pension_subpno || ' - ' || p.pension_name_nep
       else p.pension_subpno || ' - ' || p.pension_name_nep || 'को ' || r.relation_name_nep || ' ' || p.pension_sub_name_nep
      end as name
      from pensioner_registration p
      left join pensioner_relation r on r.relation_code = p.pension_relation
      where p.pension_status  = 1
      order by p.pension_subpno
      """)
  List<Tuple> getActivePensioners();

  @Query(nativeQuery = true, value = """
      select p.* from pensioner_registration p
      left join offices o on o.office_code = p.pension_bank_code
      where p.pension_subpno = :subPNo
        and (:userGroupId = 1 -- for pension office
          or (:userGroupId = 3 and o.district_code = :districtCode) -- for dtco
          or (:userGroupId = 2 and o.office_code = p.pension_bank_code) -- for banks
        )
      """)
  Optional<PensionerRegistration> hasPermissionOverPensioner(String subPNo, int userGroupId, String districtCode);

  @Query(nativeQuery = true, value = """
        with documents_info as (
          select pension_subpno, count(1) as total_documents
          from pensioner_documents
          group by pension_subpno
        )
        select mp.id, mp.pension_no, mp.pension_name_nep, mp.pension_name_eng,
        mp.pension_retire_office, mp.pension_retire_nep, mp.pension_amount_per_month, mp.stage,
        ps.scheme_name_nep, o.office_name_nep as bank_branch_name, mp.pension_date_nep,
        ot.description as bank_name, mp.pension_death_date_nep,
        coalesce(di.total_documents, 0) as total_documents, mp.pension_status, s.status_name_nep
        from pensioner_registration mp
        left join pensioner_schemes ps on ps.scheme_code = mp.pension_scheme_code
        left join offices o on o.office_code = mp.pension_bank_code
        left join office_types ot on ot.id = o.office_type_id
        left join pensioner_status s on s.id = mp.pension_status
        left join documents_info di on di.pension_subpno = mp.pension_subpno
        where mp.pension_no = :pensionNo and mp.pension_sub_pensioner = 0
      """)
  List<Tuple> getAllMainPensionerByPensionNo(String pensionNo);

  @Query(nativeQuery = true, value = """
      select pr.id, pr.pension_no, pr.pension_subpno, pr.pension_sub_name_nep,
      pr.pension_sub_name_eng, ps.scheme_name_nep, s.status_name_nep, pr.stage
      from pensioner_registration pr
      left join pensioner_schemes ps on ps.scheme_code = pr.pension_scheme_code
      left join pensioner_status s on s.id = pr.pension_status
      where pr.pension_no = :pensionNo and pr.pension_sub_pensioner = 1
      order by pr.pension_subpno
      """)
  List<Tuple> getSubPensionersByPensionNo(@Param("pensionNo") String pensionNo);

  @Query(nativeQuery = true, value = """
      select pr.id, pr.pension_no, pr.pension_name_nep, pr.pension_name_eng,
      pr.pension_retire_office, pr.pension_retire_nep,
      pr.pension_amount_per_month, o.office_name_nep as bank_branch_name,
      pr.pension_subpno, pr.pension_sub_name_nep, pr.pension_sub_name_eng,
      ps.scheme_name_nep, s.status_name_nep, pr.stage
      from pensioner_registration pr
      left join offices o on o.office_code = pr.pension_bank_code
      left join pensioner_schemes ps on ps.scheme_code = pr.pension_scheme_code
      left join pensioner_status s on s.id = pr.pension_status
      where pr.pension_no = :pensionNo
        and pr.pension_sub_pensioner = 1
        and pr.pension_name_nep is not null
        and pr.pension_sub_name_nep is not null
      order by pr.pension_subpno
      """)
  List<Tuple> getDirectSubPensioner(@Param("pensionNo") String pensionNo);

  @Query(nativeQuery = true, value = """
      select exists(
        select 1 from pensioner_registration
        where pension_subpno = :subPNo
              and pension_sub_pensioner = 1
              and pension_name_nep is not null
              and pension_sub_name_nep is not null
      )
      """)
  boolean isDirectSubPensioner(String subPNo);

  @Query(nativeQuery = true, value = """
      with documents_info as (
        select pension_subpno, count(1) as total_documents
        from pensioner_documents
        group by pension_subpno
      ),
      latest_approved_voucher_info as (
        select vm.pension_subpno, vm.new_scheme_code, ps.scheme_name_nep,vm.new_pension_end_date
        from voucher_transaction_master vm
        inner join pensioner_schemes ps on ps.scheme_code = vm.new_scheme_code
        where vm.pension_subpno = :subPNo and vm.stage = 4 and vm.voucher_type = 1 and vm.id > 142812237
        order by vm.id desc limit 1
      ),
      last_voucher_date as (
        select vm.pension_subpno, nc.nepali_date as last_voucher_date
        from voucher_transaction_master vm
        inner join nepali_calendar nc on nc.roman_date = vm.created_date::date
        where vm.pension_subpno = :subPNo and vm.stage != 100 and vm.voucher_type = 1 and vm.id > 142812237
        order by vm.id desc limit 1
      )
      select p.id, p.pension_no, p.pension_subpno, p.pension_date_nep,
      p.pension_amount_per_month, s.status_name_nep, p.stage,
      coalesce(vi.new_scheme_code, p.pension_scheme_code) as pension_scheme_code,
      coalesce(vi.scheme_name_nep, ps.scheme_name_nep) as scheme_name_nep,
      case when p.pension_sub_pensioner = 0 then p.pension_name_nep
        else p.pension_name_nep || 'को ' || r.relation_name_nep || ' ' || p.pension_sub_name_nep
      end as name_nep,
      case when p.pension_sub_pensioner = 0 then p.pension_name_eng
        else p.pension_name_eng || '''s ' || r.relation_name_eng || ' ' || p.pension_sub_name_eng
      end as name_eng,
      pp.post_name_nep, p.national_id, ot.description as bank_name,
      ot.description || ' (' || o.office_name_nep || ')' as bank_branch_name, p.pension_account_no,
      coalesce(di.total_documents, 0) as total_documents, p.other_type_code, pot.other_type_name_nep,
      coalesce(vi.new_pension_end_date, p.pension_end_date_nep) as pension_end_date,
      lvd.last_voucher_date
      from pensioner_registration p
      left join office_types ot on ot.id = p.main_bank_code
      left join offices o on o.office_code = p.pension_bank_code
      left join pensioner_schemes ps on ps.scheme_code = p.pension_scheme_code
      left join pensioner_status s on s.id = p.pension_status
      left join pensioner_other_types pot on pot.other_type_code = p.other_type_code
      left join pensioner_posts pp on pp.pensioner_post_code = p.pension_post_code
      left join pensioner_relation r on r.relation_code = p.pension_relation
      left join documents_info di on di.pension_subpno = p.pension_subpno
      left join latest_approved_voucher_info vi on vi.pension_subpno = p.pension_subpno
      left join last_voucher_date lvd on lvd.pension_subpno = p.pension_subpno
      where p.pension_subpno = :subPNo
        """)
  List<Tuple> getPensionerInfoBySubPNo(@Param("subPNo") String subPNo);

  @Query(nativeQuery = true, value = """
      with pensioner_info as (
        select pr.pension_subpno, pr.pension_sub_pensioner,
        case when pr.pension_sub_pensioner = 0 then pr.pension_name_nep else pr.pension_sub_name_nep end as pensioner_name_nep,
        case when pr.pension_sub_pensioner = 0 then pr.pension_name_eng else pr.pension_sub_name_eng end as pensioner_name_eng,
        ps.status_name_nep as pensioner_status_name_nep,
        ps.status_name_eng as pensioner_status_name_eng
        from pensioner_registration pr
        inner join pensioner_status ps on ps.id = pr.pension_status
        where pr.pension_subpno = :subPNo
      ),
      presence_info as (
        select :subPNo as pension_subpno,
        coalesce(max(present_date_eng), '1900-04-12'::date) as last_presence_date_eng,
        (current_date - coalesce(max(present_date_eng), '1900-04-12'::date)) as days_since_last_presence
        from pensioner_presence where pension_subpno = :subPNo
      ),
      next_presence_info as (
        select p.pension_subpno,
        (select nepali_date from nepali_calendar
        where roman_date = i.last_presence_date_eng + case when p.pension_sub_pensioner = 0 then 365 else 183 end
        ) as next_presence_date_nep,
        (select roman_date from nepali_calendar
        where roman_date = i.last_presence_date_eng + case when p.pension_sub_pensioner = 0 then 365 else 183 end
        ) as next_presence_date_eng
        from pensioner_info p
        inner join presence_info i on i.pension_subpno = p.pension_subpno
      )
      select
      pr.pensioner_name_nep,
      pr.pensioner_name_eng,
      pr.pensioner_status_name_nep,
      pr.pensioner_status_name_eng,
      i.last_presence_date_eng,
      (select nepali_date from nepali_calendar where roman_date = i.last_presence_date_eng) as last_presence_date_nep,
      i.days_since_last_presence,
      i.days_since_last_presence between 0 and case when pr.pension_sub_pensioner = 0 then 365 else 183 end as kyc_valid,
      npi.next_presence_date_nep,
      next_presence_date_eng
      from pensioner_info pr
      inner join presence_info i on i.pension_subpno = pr.pension_subpno
      inner join next_presence_info npi on npi.pension_subpno = pr.pension_subpno
      """)
  PensionerInfoForNagarikApp getPensionerInfoForNagarikApp(String subPNo);
}