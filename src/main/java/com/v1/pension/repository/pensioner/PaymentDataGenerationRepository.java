package com.v1.pension.repository.pensioner;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaymentDataGenerationRepository {
  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  public Collection<Map<String, Object>> getRegularPaymentInfo(
      int mainBankCode, String todayDateNep, String pensionTypeCode, String fy) {
    var sql = """
        with block_release_data as (
            select pbr.pension_subpno
            from pensioner_block_release pbr
            group by pbr.pension_subpno
            having count(*) filter (where pbr.type = 3)
                 - count(*) filter (where pbr.type in (1, 2)) < 0
        ),
        voucher_info as materialized (
            select distinct on (pension_subpno)
                   id, pension_subpno, new_pension_amount, new_pension_end_date,
                   calculation_from_date, calculation_to_date,
                   (fiscal_year = :fy and legal_case = true) as legal_case
            from voucher_transaction_master
            where voucher_type = 1 and stage = 4 and id > 142820925
            order by pension_subpno, id desc
        ),
        monthly_info as materialized (
            select calculation_from_date as last_monthly_pension_date
            from voucher_transaction_master
            where voucher_type = 2 and stage = 4 and id > 142820925
            order by id desc
            limit 1
        ),
        eligible as materialized (
            select p.pension_subpno, p.pension_end_date_nep, p.pension_sub_pensioner,
                   p.pension_amount_per_month, p.pension_gender, p.other_type_code,
                   p.pension_marital_status, p.festival_month,
                   b.balance,
                   vi.new_pension_amount, vi.calculation_from_date,
                   vi.calculation_to_date, vi.legal_case,
                   case when p.pension_sub_pensioner = 0 then p.pension_name_eng else p.pension_sub_name_eng end as account_name_eng,
                   case when p.pension_sub_pensioner = 0 then p.pension_name_nep else p.pension_sub_name_nep end as account_name_nep,
                   ot.bank_id,
                   p.pension_account_no,
                   p.id::varchar as payee_code
            from pensioner_registration p
            inner join office_types ot on ot.id = p.main_bank_code
            join mv_pensioner_balance b on b.pension_subpno = p.pension_subpno and b.balance >= 1
            left join voucher_info vi on vi.pension_subpno = p.pension_subpno
            cross join lateral (
                select pp.present_date_eng as last_present
                from pensioner_presence pp
                where pp.pension_subpno = p.pension_subpno and pp.present_date_eng is not null
                order by pp.present_date_eng desc
                limit 1
            ) pr
            where p.main_bank_code    = :mainBankCode
              and p.pension_type_code = :pensionTypeCode
              and coalesce(p.pension_status, 0) = 1
              and p.national_id is not null
              and p.national_id <> ''
              and coalesce(length(p.pension_account_no), 0) in (16, 20)
              and length(case when p.pension_sub_pensioner = 0
                              then coalesce(p.pension_name_eng, '')
                              else coalesce(p.pension_sub_name_eng, '') end) >= 4
              and coalesce(nullif(case when vi.id is not null
                                       then vi.new_pension_end_date
                                       else p.pension_end_date_nep end, ''),
                           :todayDateNep) >= :todayDateNep
              and (current_date - pr.last_present)
                  between 0 and case when p.pension_sub_pensioner = 0 then 365 else 183 end
              and not exists (select 1 from payment_excluded_pensioners x
                              where x.pension_subpno = p.pension_subpno)
              and not exists (select 1 from block_release_data brd
                              where brd.pension_subpno = p.pension_subpno)
        ),
        paid_amounts as (
            select vd.pension_subpno,
                   sum(abs(vd.total_amount)) + sum(abs(vd.tax)) as amount_paid,
                   sum(abs(vd.tax))                             as tax_paid
            from voucher_transaction_details vd
            join voucher_transaction_master vm on vm.id = vd.master_id
            where vd.id > 49044594
              and vm.voucher_type = 4
              and vm.fiscal_year  = :fy
              and vm.stage        = 4
              and vd.status in (2, 3)
              and vd.pension_subpno in (select pension_subpno from eligible)   -- <<< the fix
            group by vd.pension_subpno
        )
        select
            e.pension_subpno as sub_number,
            coalesce(e.balance, 0) as balance,
            e.calculation_from_date,
            e.calculation_to_date,
            coalesce(e.legal_case, false) as legal_case,
            e.pension_gender, e.other_type_code, e.pension_marital_status,
            e.pension_end_date_nep, e.festival_month,
            coalesce(e.new_pension_amount, e.pension_amount_per_month) as monthly_amount,
            greatest(coalesce(e.calculation_to_date, ''), coalesce(mi.last_monthly_pension_date, '')
            ) as last_monthly_pension_date,
            coalesce(pa.amount_paid, 0) as amount_paid_this_fy,
            coalesce(pa.tax_paid, 0) as tax_paid_this_fy,
            e.account_name_eng, e.account_name_nep, e.bank_id, e.pension_account_no, e.payee_code
        from eligible e
        cross join monthly_info mi
        left join paid_amounts pa on pa.pension_subpno = e.pension_subpno
        """;

    var parameters = new MapSqlParameterSource();
    parameters.addValue("mainBankCode", mainBankCode);
    parameters.addValue("todayDateNep", todayDateNep);
    parameters.addValue("pensionTypeCode", pensionTypeCode);
    parameters.addValue("fy", fy);

    return namedParameterJdbcTemplate.queryForList(sql, parameters);
  }

  public Collection<Map<String, Object>> getTameliPaymentInfo(List<String> pensionerTypes, String fy) {
    var sql = """
        with voucher_info as (
          select * from (
            select row_number() over(partition by vm.pension_subpno order by vm.id desc) as rn, vm.pension_subpno,
            case when vm.fiscal_year = :fy and vm.legal_case = true then true else false end as legal_case
            from voucher_transaction_master vm
            where vm.voucher_type = 1 and vm.stage = 4 and vm.id > 142820925
          ) where rn = 1
        ),
        paid_amounts as (
          select
          vd.pension_subpno,
          sum(abs(vd.total_amount)) + sum(abs(vd.tax)) as amount_paid,
          sum(abs(vd.tax)) as tax_paid
          from voucher_transaction_details vd
          inner join voucher_transaction_master vm on vm.id = vd.master_id
          where vd.id > 49044594
            and vm.voucher_type = 4
            and vm.fiscal_year = :fy
            and vm.stage = 4
            and vd.status in (2,3)
          group by vd.pension_subpno
        )
        select distinct vm.pension_subpno as pension_number, b.balance,
        p.pension_gender, p.other_type_code, p.pension_marital_status,
        coalesce(vi.legal_case, false) as legal_case,
        coalesce(pa.amount_paid, 0) as amount_paid_this_fy,
        coalesce(pa.tax_paid, 0) as tax_paid_this_fy,
        vm.nominee, vm.nominee_bank_code::int4 as nominee_bank_code, vm.nominee_bank_account_no, p.id::varchar as payee_code
        from voucher_transaction_master vm
        inner join pensioner_registration p on p.pension_subpno = vm.pension_subpno
        inner join mv_pensioner_balance b on b.pension_subpno = p.pension_subpno
        left join voucher_info vi on vi.pension_subpno = vm.pension_subpno
        left join paid_amounts pa on pa.pension_subpno = vm.pension_subpno
        where vm.voucher_type = 1
          and vm.stage = 4
          and vm.calculation_type_code in ('8', '9')
          and vm.nominee_bank_code is not null and vm.nominee_bank_code <> ''
          and vm.nominee_bank_account_no is not null
          and vm.nominee is not null
          and b.balance >= 1
          and p.pension_type_code in (:pensionerTypes)
        """;

    var parameters = new MapSqlParameterSource();
    parameters.addValue("pensionerTypes", pensionerTypes);
    parameters.addValue("fy", fy);

    return namedParameterJdbcTemplate.queryForList(sql, parameters);
  }
}
