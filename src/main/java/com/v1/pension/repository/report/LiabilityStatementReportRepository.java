package com.v1.pension.repository.report;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class LiabilityStatementReportRepository {
  private final JdbcTemplate jdbcTemplate;

  public List<Map<String, Object>> getDetailsData(int type) {
    if (type == 1) {
      return getPensionExpenseDetailsData();
    } else if (type == 2) {
      return getTaxExpenseDetailsData();
    } else {
      return List.of();
    }
  }

  private List<Map<String, Object>> getPensionExpenseDetailsData() {
    var sql = """
        with data as (
        	select '' as date, 'Opening' as particular, 0 as debit, 0 as credit, 0 as order_by_value

        	union

         select
         vm.start_date as date,
         case vm.voucher_type
         when 1 then 'Pension Adjustment Payable'
         when 2 then 'Monthly Pension - ' || nepali_month_name(vm.start_date)
         when 4 then 'Pension Payment'
         end as particular,
         case when vm.voucher_type in (1,2) then coalesce(sum(vd.total_amount), 0) else 0 end as debit,
         case when vm.voucher_type = 4 then coalesce(sum(abs(vd.total_amount)), 0) else 0 end as credit,
         vm.voucher_type as order_by_value
         from voucher_transaction_details vd
         inner join voucher_transaction_master vm on vm.id = vd.master_id
         where vm.voucher_type in (1,2,4)
         group by vm.start_date, vm.voucher_type
        )
        select particular, date,
        case when debit = 0 then null else debit end as debit,
        case when credit = 0 then null else credit end as credit,
        sum(debit - credit) over (order by order_by_value) as balance
        from data order by order_by_value
        """;

    return jdbcTemplate.queryForList(sql);
  }

  private List<Map<String, Object>> getTaxExpenseDetailsData() {
    var sql = """
        with data as (
        	select '' as date, 'Opening' as particular, 0 as debit, 0 as credit, 0 as order_by_value

        	union

        	select
        	mm2.calculation_date as date,
        	'Pension Tax Payable - ' || nepali_month_name(mm2.month) as particular,
        	0 as debit,
        	coalesce(sum(md2.tax), 0) as credit,
        	3 as order_by_value
        	from monthly_pension_calculation_details md2
        	inner join monthly_pension_calculation_master mm2 on mm2.id = md2.master_id
        	group by mm2.month, mm2.calculation_date
        )
        select particular, date,
        case when debit = 0 then null else debit end as debit,
        case when credit = 0 then null else credit end as credit,
        sum(debit - credit) over (order by order_by_value) as balance
        from data order by order_by_value
        """;

    return jdbcTemplate.queryForList(sql);
  }
}
