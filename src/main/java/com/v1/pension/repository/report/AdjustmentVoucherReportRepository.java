package com.v1.pension.repository.report;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AdjustmentVoucherReportRepository {
  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  public Map<String, Object> getAdjustmentVoucherPreviewMasterData(long id) {
    var sql = """
        select vm.voucher_no, vm.pension_subpno,
            case when pr.pension_sub_pensioner = 0 then pr.pension_name_nep
              else pr.pension_name_nep || 'को ' || r.relation_name_nep || ' ' || pr.pension_sub_name_nep
            end as pension_name,
            coalesce(pr.pension_phone_no, '') as pension_phone_no,
            coalesce(pr.pension_address, '') as pension_address,
            coalesce(pr.pension_retire_office, '') as pension_retire_office,
            coalesce(pr.pension_retire_nep, '') as pension_retire_nep,
            coalesce(pr.pension_date_nep, '') as pension_date_nep,
            vm.new_pension_amount,
            pct.calculation_name_nep,
            ot.description || ' (' || o.office_name_nep || ')' as bank_name,
            vm.statement_date,
            coalesce(pr.pension_account_no, '') as account_no,
            coalesce(pt.type_name_nep, '') as type_name_nep,
            coalesce(pc.category_nep, '') as category_nep,
            coalesce(pl.level_name_nep, '') as level_name_nep,
            coalesce(pp.post_name_nep, '') as post_name_nep,
            vm.new_pension_start_date,
            pr.pension_amount_per_month,
            vm.new_pension_end_date, ps.scheme_name_nep, vm.remarks, vm.calculation_amount, vm.total_adjustment_amt,
            vm.total_payment_amt, vm.tax, vm.statement_amount, vm.transfer_to_govt_adj,
            vm.payment_from_bank_ac, vm.calculation_from_date, vm.calculation_to_date, vm.additional_pay_amount,
            vm.voucher_balance, vm.statement_balance,
            vm.nominee, b.name as nominee_bank_name, vm.nominee_bank_account_no, vm.nominee_contact
            from voucher_transaction_master vm
            inner join pension_calculation_type pct on pct.calculation_code = vm.calculation_type_code
            inner join pensioner_registration pr on pr.pension_subpno = vm.pension_subpno
            inner join office_types ot on ot.id = pr.main_bank_code
            inner join offices o on o.office_code = pr.pension_bank_code
            left join pensioner_posts pp on pp.pensioner_post_code = pr.pension_post_code
            left join pensioner_types pt on pt.type_code = pr.pension_type_code
            left join pensioner_categories pc on pc.pensioner_category_code = pr.pension_category_code
            left join pensioner_levels pl on pl.pensioner_level_code = pr.pension_level_code
            left join pensioner_schemes ps on ps.scheme_code = vm.new_scheme_code
            left join pensioner_relation r on r.relation_code = pr.pension_relation
            left join banks b on b.code = vm.nominee_bank_code
            where vm.id = :id
            """;

    var parameters = new MapSqlParameterSource();
    parameters.addValue("id", id);
    return namedParameterJdbcTemplate.queryForMap(sql, parameters);
  }

  public List<Map<String, Object>> getAdjustmentVoucherPreviewDetailData(long id) {
    var sql = """
        select vd.start_date, vd.end_date, vd.receivable_amount,
        vd.increment, vd.receive_amount, vd.total_amount, vd.difference, vd.period,
        case when vd.period_type = 1 then 'D' else 'M' end as period_type
        from voucher_transaction_details vd
        where vd.master_id = :id
        """;

    var parameters = new MapSqlParameterSource();
    parameters.addValue("id", id);
    return namedParameterJdbcTemplate.queryForList(sql, parameters);
  }

  public Map<String, Object> getAdjustmentVoucherPreviewFooterData(long id) {
    var sql = """
        with all_approved_data as (
          select row_number() over(partition by d.receive_stage order by d.id desc) rn, d.* from (
            select fdd.id, fdd.master_id,
              fdd.receive_stage,
              u.full_name as approve_by,
              p.name_nep as approve_post,
              fdd.action_date || ' | ' || to_char(fdd.created_date, 'HH:MI:SS AM') AS approve_date
              from forwarded_data_details fdd
              left join users u on u.id = fdd.receive_user_id
              left join user_posts p on p.id = u.post_id
              where fdd.form_type_id = 1 and fdd.master_id = :id and fdd.action = 1
          ) d
        ),
        approved_summary as (
          select d.master_id,
          max(case when d.receive_stage = 3 then d.approve_by end) as approve_by_pmo,
            max(case when d.receive_stage = 3 then d.approve_post end) as approve_post_pmo,
            max(case when d.receive_stage = 3 then d.approve_date end) AS approve_date_pmo,
            max(case when d.receive_stage = 11 then d.approve_by end) as approve_by_dtco,
            max(case when d.receive_stage = 11 then d.approve_post end) as approve_post_dtco,
            max(case when d.receive_stage = 11 then d.approve_date end) AS approve_date_dtco
          from all_approved_data d where d.rn = 1
          group by d.master_id
        )
        select u.full_name as created_by,
        p.name_nep as created_post,
        nc.nepali_date || ' | ' || to_char(vm.created_date, 'HH:MI:SS AM') as created_date,
        case when vm.office_code = '00001' then fi.approve_by_pmo else approve_by_dtco end as approve_by,
        case when vm.office_code = '00001' then fi.approve_post_pmo else approve_post_dtco end as approve_post,
        case when vm.office_code = '00001' then fi.approve_date_pmo else approve_date_dtco end as approve_date
        from voucher_transaction_master vm
        inner join users u on u.username = vm.created_by
        left join user_posts p on p.id = u.post_id
        inner join nepali_calendar nc on nc.roman_date = vm.created_date::date
        left join approved_summary fi on fi.master_id = vm.id
        where vm.id = :id
            """;

    var parameters = new MapSqlParameterSource();
    parameters.addValue("id", id);
    return namedParameterJdbcTemplate.queryForMap(sql, parameters);
  }
}
