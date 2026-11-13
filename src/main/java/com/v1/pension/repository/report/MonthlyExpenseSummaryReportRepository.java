package com.v1.pension.repository.report;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MonthlyExpenseSummaryReportRepository {
  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  public List<Map<String, Object>> getSummaryData(String fy) {
    var query = """
        with months_info as (
          select 1 as id, 4 as month_id, 'साउन' as month_name union
          select 2 as id, 5 as month_id, 'भदौ' as month_name union
          select 3 as id, 6 as month_id, 'असोज' as month_name union
          select 4 as id, 7 as month_id, 'कात्तिक' as month_name union
          select 5 as id, 8 as month_id, 'मंसिर' as month_name union
          select 6 as id, 9 as month_id, 'पुष' as month_name union
          select 7 as id, 10 as month_id, 'माघ' as month_name union
          select 8 as id, 11 as month_id, 'फागुन' as month_name union
          select 9 as id, 12 as month_id, 'चैत' as month_name union
          select 10 as id, 1 as month_id, 'बैशाख' as month_name union
          select 11 as id, 2 as month_id, 'जेठ' as month_name union
          select 12 as id, 3 as month_id, 'असार' as month_name
        ),
        payment_info as (
          select
            substring(vm.statement_date from 6 for 2)::int as month_id,
            sum(case when pr.pension_type_code = '1' then abs(vd.total_amount) + abs(vd.tax) else 0 end) as nijamati_expense,
            sum(case when pr.pension_type_code = '2' then abs(vd.total_amount) + abs(vd.tax) else 0 end) as police_expense,
            sum(case when pr.pension_type_code = '3' then abs(vd.total_amount) + abs(vd.tax) else 0 end) as teacher_expense,
            sum(case when pr.pension_type_code = '4' then abs(vd.total_amount) + abs(vd.tax) else 0 end) as army_expense,
            sum(abs(vd.total_amount) + abs(vd.tax)) as total_expense
          from voucher_transaction_details vd
          inner join voucher_transaction_master vm on vm.id = vd.master_id
          inner join pensioner_registration pr on pr.pension_subpno = vd.pension_subpno
          where
            vm.fiscal_year = :fy
            and vm.voucher_type = 4
            and vm.stage = 4
            and vd.status in (2,3)
            and pr.pension_type_code in ('1', '2', '3', '4')
          group by substring(vm.statement_date from 6 for 2)::int
        )
        select mi.month_name,
        round(coalesce(pi.nijamati_expense, 0)::numeric, 2)::float as nijamati_expense,
        round(coalesce(pi.police_expense, 0)::numeric, 2)::float as police_expense,
        round(coalesce(pi.teacher_expense, 0)::numeric, 2)::float as teacher_expense,
        round(coalesce(pi.army_expense, 0)::numeric, 2)::float as army_expense,
        round(coalesce(pi.total_expense, 0)::numeric, 2)::float as total_expense_monthly
        from payment_info pi
        right join months_info mi on mi.month_id = pi.month_id
        order by mi.id
        """;

    var parameters = new MapSqlParameterSource();
    parameters.addValue("fy", fy);
    return namedParameterJdbcTemplate.queryForList(query, parameters);
  }
}
