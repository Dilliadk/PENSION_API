package com.v1.pension.repository.report;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class PensionStatementReportRepository {
  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  public Map<String, Object> getMasterData(String subPNo) {
    var sql = """
        with latest_voucher_info as (
          select vm.pension_subpno, vm.new_scheme_code, ps.scheme_name_nep, vm.new_pension_amount
          from voucher_transaction_master vm
          inner join pensioner_schemes ps on ps.scheme_code = vm.new_scheme_code
          where vm.pension_subpno = :subPNo and vm.stage = 4 and vm.voucher_type = 1 and vm.id > 142812237
          order by vm.id desc limit 1
        )
        select coalesce(pt.type_name_nep, '') as type_name_nep,
        coalesce(pp.post_name_nep, '') as post_name_nep, p.pension_subpno,
        coalesce(p.pension_date_nep, '') as pension_date_nep,
        coalesce(vi.new_pension_amount, p.pension_amount_per_month) as pension_amount_per_month,
        coalesce(vi.scheme_name_nep, ps.scheme_name_nep) as scheme_name_nep,
        ot.name || ' (' || o.office_name_nep || ')' as bank_name,
        case when p.pension_sub_pensioner = 0 then p.pension_name_nep
        else p.pension_name_nep || 'को ' || r.relation_name_nep || ' ' || p.pension_sub_name_nep
        end as pension_name_nep,
        case when p.pension_sub_pensioner = 0 then p.pension_name_eng else p.pension_sub_name_eng end as pension_name_eng,
        p.pension_account_no, p.pension_phone_no
        from pensioner_registration p
        left join pensioner_types pt on pt.type_code = p.pension_type_code
        left join pensioner_schemes ps on ps.scheme_code = p.pension_scheme_code
        left join offices o on o.office_code = p.pension_bank_code
        left join office_types ot on ot.id = o.office_type_id
        left join pensioner_posts pp on pp.pensioner_post_code = p.pension_post_code
        left join pensioner_relation r on r.relation_code = p.pension_relation
        left join latest_voucher_info vi on vi.pension_subpno = p.pension_subpno
        where p.pension_subpno = :subPNo
            """;

    var parameters = new MapSqlParameterSource();
    parameters.addValue("subPNo", subPNo);
    return namedParameterJdbcTemplate.queryForMap(sql, parameters);
  }

  String statementSql = """
        with data as (
          select '' as date, 'Opening' as particular, 0 as payable, 0 as paid, 0 as master_id, 0 as group_key

          union

          -- adjustment vouchers amount
          select
          nc.nepali_date as date,
          'Pension Adjustment - ' || vm.calculation_from_date || ' to ' || vm.calculation_to_date as particular,
          coalesce(vm.total_payment_amt, 0) as payable,
          0 as paid,
          vm.id as master_id,
          1 as group_key
          from voucher_transaction_master vm
          inner join nepali_calendar nc on nc.roman_date = vm.created_date::date
          where vm.pension_subpno = :subPNo and vm.voucher_type = 1 and vm.stage = 4

          union

          -- monthly, reject, increment and payments
          select
          vm.statement_date as date,
          case
            when vm.voucher_type = 2 then 'Monthly Pension - ' || nepali_month_name(vm.statement_date)
            when vm.voucher_type = 3 then 'Increment Calculation'
            when vm.voucher_type = 5 or vd.status = 4 then 'Payment Rejected'
            when vm.voucher_type = 4 and vm.particulars = 'Adjustment: NIL Balance upto date' then 'Adjustment: NIL Balance'
            when vm.voucher_type = 4 then 'Payment'
          end as particular,
          case when vm.voucher_type in (2,3,5) then coalesce(vd.total_amount + vd.tax, 0) else 0 end as payable,
          case when vm.voucher_type in (4) then coalesce(vd.total_amount, 0) else 0 end as paid,
          vm.id as master_id,
          2 as group_key
          from voucher_transaction_details vd
          inner join voucher_transaction_master vm on vm.id = vd.master_id
          where vd.pension_subpno = :subPNo and vm.voucher_type in (2,3,4,5) and vm.stage = 4
          and (vm.particulars is null or vm.particulars <> 'Monthly pension-Tax')

          union

          -- Rejected Payments Revert
          select
          vm.statement_date as date,
          'Rejected Payment Revert' as particular,
          coalesce(vd.receivable_amount * -1, 0) as payable,
          0 as paid,
          vm.id as master_id,
          10 as group_key
          from voucher_transaction_details vd
          inner join voucher_transaction_master vm on vm.id = vd.master_id
          where vd.pension_subpno = :subPNo and vm.voucher_type = 4 and vm.stage = 4 and vd.status = 4

          union

          -- payment tax
          select
          vm3.statement_date as date,
          'Pension Payment Tax' as particular,
          0 as payable,
          case when particulars = 'Adjustment: NIL Balance upto date' then 0 else coalesce(vd3.tax, 0) end as paid,
          vm3.id as master_id,
          5 as group_key
          from voucher_transaction_details vd3
          inner join voucher_transaction_master vm3 on vm3.id = vd3.master_id
          where vd3.pension_subpno = :subPNo and vm3.voucher_type = 4 and vm3.stage = 4
          and (vm3.particulars is null or vm3.particulars <> 'Monthly pension-Tax')
        )
      """;

  public List<Map<String, Object>> getDetailsData(String subPNo, String startDate, String endDate) {
    if (!StringUtils.hasText(startDate))
      startDate = "2060-01-01";
    if (!StringUtils.hasText(endDate))
      endDate = "2999-12-32";

    var sql = statementSql + " " + """
        select master_id, particular, date,
        case when payable = 0 then null else payable end as payable,
        case when paid = 0 then null else paid end as paid,
        sum(payable + paid) over (order by date, master_id, group_key) as balance
        from (
          select 0 as master_id, 'Opening' as particular, '' as date,
          case when sum(payable + paid) > 0 then sum(payable + paid) else 0 end as payable,
          case when sum(payable + paid) < 0 then sum(payable + paid) else 0 end as paid,
          0 as group_key
          from data where date < :startDate

          union

          select master_id, particular, date, payable, paid, group_key
          from data
          where date between :startDate and :endDate
        ) t
        order by date, master_id, group_key
            """;

    var parameters = new MapSqlParameterSource();
    parameters.addValue("subPNo", subPNo);
    parameters.addValue("startDate", startDate);
    parameters.addValue("endDate", endDate);
    return namedParameterJdbcTemplate.queryForList(sql, parameters);
  }

  public double getStatementBalanceAmtForPensioner(String subPNo) {
    var sql = statementSql + " " + """
        select sum(payable + paid) as balance from data
        """;

    var parameters = new MapSqlParameterSource();
    parameters.addValue("subPNo", subPNo);
    var data = namedParameterJdbcTemplate.queryForMap(sql, parameters);
    return (double) data.get("balance");
  }

  public Map<String, Object> getEligibilityData(String subPNo, String fy, String todayDateNep) {
    var sql = """
          with info as(
            select :subPNo as sub_pno,
            (
              select (current_date -
              (select coalesce((select present_date_eng
              from pensioner_presence where pension_subpno = :subPNo and present_date_eng is not null
              order by present_date_eng desc limit 1), '1900-04-12')))
            ) as days_since_last_present,
            (
              select
              count(case when pbr.type = 3 then 1 end) -
              count(case when pbr.type in (1,2) then 1 end)
              from pensioner_block_release pbr
              where pbr.pension_subpno = :subPNo
            ) as release_to_block_difference
          ),
          voucher_info as (
            select * from (
              select row_number() over(partition by pension_subpno order by id desc) as rn, id,
              pension_subpno, new_pension_amount, new_pension_end_date, calculation_from_date, calculation_to_date,
              case when fiscal_year = :fy and legal_case = true then true else false end as legal_case
              from voucher_transaction_master
                where pension_subpno = :subPNo and voucher_type = 1 and stage = 4 and id > 142820925
            ) where rn = 1
          )
          select
          (pr.national_id is not null and pr.national_id != '') as have_national_id,
          (coalesce(length(pr.pension_account_no), 0) in (16, 20) and
            length((case when pr.pension_sub_pensioner = 0 then coalesce(pr.pension_name_eng, '')
              else coalesce(pr.pension_sub_name_eng, '') end)
            ) >= 4
          ) as is_bank_account_verified,
          (i.days_since_last_present is not null and i.days_since_last_present between 0 and case when pr.pension_sub_pensioner = 0 then 365 else 183 end) as is_kyc_valid,
          (i.release_to_block_difference >= 0) as is_not_blocked,
          (
                case when vi.id is not null then vi.new_pension_end_date else pr.pension_end_date_nep end is null
                or case when vi.id is not null then vi.new_pension_end_date else pr.pension_end_date_nep end = ''
                or case when vi.id is not null then vi.new_pension_end_date else pr.pension_end_date_nep end >= :todayDateNep
          )
          as is_end_date_valid,
          (coalesce(pr.pension_status, 0) = 1) as is_pensioner_active,
          case when
            case when vi.id is not null then vi.new_pension_end_date else pr.pension_end_date_nep end is null
            or case when vi.id is not null then vi.new_pension_end_date else pr.pension_end_date_nep end = ''
          then '(Open Ended)'
          else '(Closed Ended: ' ||
            case when vi.id is not null then vi.new_pension_end_date else pr.pension_end_date_nep end
            || ')'
          end as end_date_info,
          (select nepali_date from nepali_calendar where roman_date =
            (
            select coalesce(to_char(max(present_date_eng) + case when pr.pension_sub_pensioner = 0 then 365 else 183 end, 'YYYY-MM-DD'), '1900-04-12')
            from pensioner_presence where pension_subpno = :subPNo
            )::date
          ) as next_presence_date,
          (select file_data
          from pensioner_presence where pension_subpno = :subPNo
          order by present_date_eng desc limit 1
          ) as image_data,
           ps.status_name_nep
          from pensioner_registration pr
          inner join pensioner_status ps on ps.id = pr.pension_status
          left join info i on i.sub_pno = pr.pension_subpno
          left join voucher_info vi on vi.pension_subpno = pr.pension_subpno
          where pr.pension_subpno = :subPNo
        """;

    var parameters = new MapSqlParameterSource();
    parameters.addValue("subPNo", subPNo);
    parameters.addValue("fy", fy);
    parameters.addValue("todayDateNep", todayDateNep);
    return namedParameterJdbcTemplate.queryForMap(sql, parameters);
  }
}
