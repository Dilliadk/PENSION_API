package com.v1.pension.repository.report;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.v1.pension.dto.other.ReportDto;
import com.v1.pension.exception.ResourceNotFoundException;
import com.v1.pension.repository.system.NepaliCalendarRepository;

@Repository
@RequiredArgsConstructor
public class OtherReportRepository {
  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
  private final NepaliCalendarRepository nepaliCalendarRepository;

  public List<Map<String, Object>> getDailyTaskSummary(String officeCode, ReportDto dto) {
    var dateFrom = nepaliCalendarRepository.findById(dto.getDateFrom())
        .orElseThrow(() -> new ResourceNotFoundException("Date not found"));
    var dateTo = nepaliCalendarRepository.findById(dto.getDateTo())
        .orElseThrow(() -> new ResourceNotFoundException("Date not found"));

    var sql = """
        with office_users as (
          select distinct m.user_id
          from user_role_mapping m
          where m.status = 1 and m.office_code = :officeCode and m.role_id not in (1, 11, 13)
        ),
        pensioner_entry_info as (
          select created_by,
          sum(case when pension_sub_pensioner = 0 then 1 else 0 end) as mp_entry_count,
          sum(case when pension_sub_pensioner = 1 then 1 else 0 end) as sp_entry_count
          from pensioner_registration
          where created_date::date between :dateFrom and :dateTo
          group by created_by
        ),
        voucher_entry_count as (
          select created_by,
          sum(case when calculation_type_code = '6' then 1 else 0 end) as extra_service_entry_count,
          sum(case when calculation_type_code = '2' then 1 else 0 end) as adjustment_entry_count,
          sum(case when calculation_type_code in ('8', '9') then 1 else 0 end) as tameli_entry_count
          from voucher_transaction_master
          where id > 142812237 and stage != 100 and created_date::date between :dateFrom and :dateTo
          group by created_by
        ),
        document_info as (
          select created_by, count(1) as document_upload_count
          from pensioner_documents
          where created_date::date between :dateFrom and :dateTo
          group by created_by
        ),
        forward_info as (
          select source_user_id, count(1) as forward_count
          from forwarded_data_details
          where action = 1 and created_date::date between :dateFrom and :dateTo
          group by source_user_id
        ),
        approve_info as (
          select receive_user_id, count(1) as approve_count
          from forwarded_data_details
          where action = 1 and created_date::date between :dateFrom and :dateTo
          group by receive_user_id
        ),
        final_info as (
          select
          u.full_name,
          coalesce(pei.mp_entry_count, 0)::int4 as mp_entry_count,
          coalesce(pei.sp_entry_count, 0)::int4 as sp_entry_count,
          coalesce(vec.extra_service_entry_count, 0)::int4 as extra_service_entry_count,
          coalesce(vec.adjustment_entry_count, 0)::int4 as adjustment_entry_count,
          coalesce(vec.tameli_entry_count, 0)::int4 as tameli_entry_count,
          coalesce(di.document_upload_count, 0)::int4 as document_upload_count,
          coalesce(fi.forward_count, 0)::int4 as forward_count,
          coalesce(ai.approve_count, 0)::int4 as approve_count,
          (
            coalesce(pei.mp_entry_count, 0) +
            coalesce(pei.sp_entry_count, 0) +
            coalesce(vec.extra_service_entry_count, 0) +
            coalesce(vec.adjustment_entry_count, 0) +
            coalesce(vec.tameli_entry_count, 0) +
            coalesce(di.document_upload_count, 0) +
            coalesce(fi.forward_count, 0) +
            coalesce(ai.approve_count, 0)
          )::int4 as total_task_count,
          null as remarks
          from users u
          inner join office_users ou on ou.user_id = u.id
          left join pensioner_entry_info pei on pei.created_by = u.username
          left join voucher_entry_count vec on vec.created_by = u.username
          left join document_info di on di.created_by = u.username
          left join forward_info fi on fi.source_user_id = u.id
          left join approve_info ai on ai.receive_user_id = u.id
        )
        select * from final_info order by total_task_count desc
          """;

    var parameters = new MapSqlParameterSource();
    parameters.addValue("officeCode", officeCode);
    parameters.addValue("dateFrom", dateFrom.getRomanDate());
    parameters.addValue("dateTo", dateTo.getRomanDate());
    return namedParameterJdbcTemplate.queryForList(sql, parameters);
  }

  public List<Map<String, Object>> getDailyTaskDetails(String officeCode, ReportDto dto) {
    var dateFrom = nepaliCalendarRepository.findById(dto.getDateFrom())
        .orElseThrow(() -> new ResourceNotFoundException("Date not found"));
    var dateTo = nepaliCalendarRepository.findById(dto.getDateTo())
        .orElseThrow(() -> new ResourceNotFoundException("Date not found"));

    var sql = """
        with office_users as (
          select distinct m.user_id
          from user_role_mapping m
          where m.status = 1 and m.office_code = :officeCode and m.role_id not in (1, 11, 13)
        )
        select
        nc.nepali_date as created_date_nep,
        vm.pension_subpno,
        vm.voucher_no,
        pt.type_name_nep,
        case when pr.pension_sub_pensioner = 0 then pr.pension_name_nep else pr.pension_sub_name_nep end as pensioner_name,
        pp.post_name_nep,
        pct.calculation_name_nep,
        pr.pension_phone_no
        from voucher_transaction_master vm
        inner join users u on u.username = vm.created_by
        inner join office_users ou on ou.user_id = u.id
        inner join nepali_calendar nc on nc.roman_date = vm.created_date::date
        inner join pensioner_registration pr on pr.pension_subpno = vm.pension_subpno
        inner join pensioner_types pt on pt.type_code = pr.pension_type_code
        inner join pension_calculation_type pct on pct.calculation_code = vm.calculation_type_code
        left join pensioner_posts pp on pp.pensioner_post_code = pr.pension_post_code
        where vm.id > 142812237
          and vm.stage != 100
          and vm.voucher_type = 1
          and vm.created_date::date between :dateFrom and :dateTo
        order by vm.created_date, vm.id
              """;

    var parameters = new MapSqlParameterSource();
    parameters.addValue("officeCode", officeCode);
    parameters.addValue("dateFrom", dateFrom.getRomanDate());
    parameters.addValue("dateTo", dateTo.getRomanDate());
    return namedParameterJdbcTemplate.queryForList(sql, parameters);
  }

  public List<Map<String, Object>> getManachamalDetails(String fy) {
    var sql = """
        with manachamal_payments_info as (
          select vd.pension_subpno, sum(vd.total_amount) as paid_amount
          from voucher_transaction_details vd
          inner join voucher_transaction_master vm on vm.id = vd.master_id
          where vm.id > 142812237
            and vm.voucher_type = 4 and vm.stage = 4 and vd.status in (2,3)
            and vd.fiscal_year = :fy and vm.fiscal_year = :fy
            and vm.payment_type = 3
          group by vd.pension_subpno
        )
        select
        pt.type_name_nep,
        pr.pension_subpno,
        pr.pension_name_nep as pensioner_name,
        vm.nominee,
        vm.additional_pay_amount as payable,
        abs(coalesce(mpi.paid_amount, 0)) as paid,
        '' as remarks
        from voucher_transaction_master vm
        inner join pensioner_registration pr on pr.pension_subpno = vm.pension_subpno
        inner join pensioner_types pt on pt.type_code = pr.pension_type_code
        left join manachamal_payments_info mpi on mpi.pension_subpno = vm.pension_subpno
        where vm.id > 142812237
          and vm.stage = 4
          and vm.voucher_type = 1
          and vm.new_scheme_code = '12'
          and pr.pension_status in (1,2)
        order by vm.created_date, vm.id
                """;

    var parameters = new MapSqlParameterSource();
    parameters.addValue("fy", fy);
    return namedParameterJdbcTemplate.queryForList(sql, parameters);
  }

  public List<Map<String, Object>> getManachamalSummary() {
    var currentDate = LocalDate.now();
    var npDate = nepaliCalendarRepository.findByRomanDate(currentDate)
        .orElseThrow(() -> new ResourceNotFoundException("Nepali date not found"));
    var currentMonthStart = String.format("%d-%02d-%02d", npDate.getNepaliYear(), npDate.getNepaliMonth(), 1);
    var currentMonthEnd = String.format("%d-%02d-%02d", npDate.getNepaliYear(), npDate.getNepaliMonth(),
        npDate.getNepaliDaysInMonth());

    var sql = """
          with manachamal_payments_info as (
          select pr.pension_type_code, abs(sum(vd.total_amount)) as this_month_payment
          from voucher_transaction_details vd
          inner join voucher_transaction_master vm on vm.id = vd.master_id
          inner join nepali_calendar nc on nc.roman_date = vm.created_date::date
          inner join pensioner_registration pr on pr.pension_subpno = vd.pension_subpno
          where vm.id > 142812237
            and vm.voucher_type = 4 and vm.stage = 4 and vd.status in (2,3)
            and vm.payment_type = 3
            and nc.nepali_date between :currentMonthStart and :currentMonthEnd
          group by pr.pension_type_code
        ),
        info as (
          select
          pr.pension_type_code,
          count(1)::int4 as total_count,
          sum(case when nc.nepali_date between :currentMonthStart and :currentMonthEnd then 1 else 0 end)::int4 as this_month_count,
          round(avg(vm.additional_pay_amount)::numeric, 2)::float8 as average_rate
          from voucher_transaction_master vm
          inner join pensioner_registration pr on pr.pension_subpno = vm.pension_subpno
          inner join nepali_calendar nc on nc.roman_date = vm.created_date::date
          where vm.id > 142812237
            and vm.stage = 4
            and vm.voucher_type = 1
            and vm.new_scheme_code = '12'
          group by pr.pension_type_code
        )
        select
        pt.type_name_nep,
        coalesce(i.total_count, 0) as total_count,
        coalesce(i.this_month_count, 0) as this_month_count,
        coalesce(i.average_rate, 0) as average_rate,
        round(coalesce(mpi.this_month_payment, 0)::numeric, 2)::float8 as this_month_expense
        from pensioner_types pt
        left join info i on i.pension_type_code = pt.type_code
        left join manachamal_payments_info mpi on mpi.pension_type_code = i.pension_type_code
        where pt.type_code in ('1', '2', '3', '4')
        order by pt.type_code
          """;

    var parameters = new MapSqlParameterSource();
    parameters.addValue("currentMonthStart", currentMonthStart);
    parameters.addValue("currentMonthEnd", currentMonthEnd);
    return namedParameterJdbcTemplate.queryForList(sql, parameters);
  }
}
