package com.v1.pension.repository.report;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.v1.pension.dto.other.ReportDto;
import com.v1.pension.exception.ResourceNotFoundException;
import com.v1.pension.repository.system.NepaliCalendarRepository;
import com.v1.pension.service.other.NrbService;
import com.v1.pension.utils.CurrentUser;

@Repository
@RequiredArgsConstructor
public class PensionerReportRepository {
  private final JdbcTemplate jdbcTemplate;
  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
  private final NepaliCalendarRepository nepaliCalendarRepository;
  private final NrbService nrbService;

  public Map<String, Object> getCategoryName(String categoryCode) {
    var sql = """
        select pensioner_category_code || ' - ' || category_nep as category_name
        from pensioner_categories where pensioner_category_code = :categoryCode
        """;

    var parameters = new MapSqlParameterSource();
    parameters.addValue("categoryCode", categoryCode);
    return namedParameterJdbcTemplate.queryForMap(sql, parameters);
  }

  public List<Map<String, Object>> getPensionersByCategory(String categoryCode) {
    var sql = """
          select p.id, p.pension_no, p.pension_subpno, p.pension_date_nep, p.pension_amount_per_month,
          p.pension_sub_pensioner, p.pension_name_nep, p.pension_sub_name_nep, p.pension_status
          from pensioner_registration p
          where p.pension_category_code = :categoryCode
        """;

    var parameters = new MapSqlParameterSource();
    parameters.addValue("categoryCode", categoryCode);
    return namedParameterJdbcTemplate.queryForList(sql, parameters);
  }

  public List<Map<String, Object>> getBankBranchWisePensionerReport(int mainBankId, String bankBranchCode) {
    var sql = """
        with voucher_info as (
          select * from (
            select row_number() over(partition by pension_subpno order by id desc) as rn, id,
            pension_subpno, new_pension_amount
            from voucher_transaction_master
            where voucher_type = 1 and stage = 4 and id > 142812237
          ) where rn = 1
        )
        select
        ot.description as bank_name,
        o.branch_code || ' - ' || o.office_name_nep as branch_name,
        sum(case when pr.pension_type_code = '1' then 1 else 0 end)::int4 as nijamati_count,
        sum(case when pr.pension_type_code = '2' then 1 else 0 end)::int4 as police_count,
        sum(case when pr.pension_type_code = '3' then 1 else 0 end)::int4 as teacher_count,
        sum(case when pr.pension_type_code = '4' then 1 else 0 end)::int4 as army_count,
        count(1)::int4 as branch_wise_total,
        round(sum(coalesce(vi.new_pension_amount, pr.pension_amount_per_month))::numeric, 2)::float8
        as branch_wise_monthly_pension,
        null as remarks
        from pensioner_registration pr
        inner join office_types ot on ot.id = pr.main_bank_code
        inner join offices o on o.office_code = pr.pension_bank_code
        inner join pensioner_types pt on pt.type_code = pr.pension_type_code
        left join voucher_info vi on vi.pension_subpno = pr.pension_subpno
        where
          pr.pension_type_code in ('1', '2', '3', '4')
          and pr.pension_status in (1,2,3)
          and pr.main_bank_code = :mainBankId
          and (:bankBranchCode = 'All' or pr.pension_bank_code = :bankBranchCode)
        group by ot.description, o.branch_code || ' - ' || o.office_name_nep
        order by 1,2
          """;

    var parameters = new MapSqlParameterSource();
    parameters.addValue("mainBankId", mainBankId);
    parameters.addValue("bankBranchCode", bankBranchCode);

    return namedParameterJdbcTemplate.queryForList(sql, parameters);
  }

  public List<Map<String, Object>> getTypeWiseCountSummary(String provinceCode, String districtCode) {
    var currentDate = LocalDate.now();
    var npDate = nepaliCalendarRepository.findByRomanDate(currentDate)
        .orElseThrow(() -> new ResourceNotFoundException("Nepali date not found"));
    var currentMonthStart = String.format("%d-%02d-%02d", npDate.getNepaliYear(), npDate.getNepaliMonth(), 1);
    var currentMonthEnd = String.format("%d-%02d-%02d", npDate.getNepaliYear(), npDate.getNepaliMonth(),
        npDate.getNepaliDaysInMonth());

    var sql = """
        with voucher_info as (
          select * from (
            select row_number() over(partition by pension_subpno order by id desc) as rn, id,
            pension_subpno, new_pension_amount
            from voucher_transaction_master
            where voucher_type = 1 and stage = 4 and id > 142812237
          ) where rn = 1
        ),
        lagat_katta_info as (
          select p.code as province_code, d.code as district_code,
          pr.pension_type_code, count(1)::int4 as lagat_katta_count
          from voucher_transaction_master vm
          inner join nepali_calendar nc on nc.roman_date = vm.created_date::date
          inner join pensioner_registration pr on pr.pension_subpno = vm.pension_subpno
          inner join offices o on o.office_code = pr.pension_bank_code
          inner join districts d on d.code = o.district_code
          inner join provinces p on p.code = d.province
          where vm.id > 142812237 and vm.voucher_type = 1 and vm.stage = 4
            and vm.calculation_type_code in ('8', '9')
            and nc.nepali_date between :currentMonthStart and :currentMonthEnd
            and (:provinceCode = 'All' or d.province = :provinceCode)
            and (:districtCode = 'All' or o.district_code = :districtCode)
          group by p.code, d.code, pr.pension_type_code
        ),
        info as (
          select
          p.code as province_code, p.name_nep as province_name,
          d.code as district_code, d.name as district_name,
          pr.pension_type_code, pt.type_name_nep,
          sum(case when nc.nepali_date < :currentMonthStart then 1 else 0 end)::int4 as total_pensioners_until_prev_month,
          sum(case when nc.nepali_date between :currentMonthStart and :currentMonthEnd then 1 else 0 end)::int4 as new_pensioners_this_month,
          count(1)::int4 as total_pensioners,
          round(sum(coalesce(vi.new_pension_amount, pr.pension_amount_per_month))::numeric, 2)::float8
          as type_wise_monthly_pension,
          null as remarks
          from pensioner_registration pr
          inner join pensioner_types pt on pt.type_code = pr.pension_type_code
          inner join offices o on o.office_code = pr.pension_bank_code
          inner join districts d on d.code = o.district_code
          inner join provinces p on p.code = d.province
          inner join nepali_calendar nc on nc.roman_date = pr.created_date::date
          left join voucher_info vi on vi.pension_subpno = pr.pension_subpno
          where
            pr.pension_type_code in ('1', '2', '3', '4')
            and pr.pension_status in (1,2,3)
            and (pr.main_bank_code is not null and pr.main_bank_code != 0)
            and (pr.pension_bank_code is not null and pr.pension_bank_code != '')
            and (:provinceCode = 'All' or d.province = :provinceCode)
            and (:districtCode = 'All' or o.district_code = :districtCode)
          group by p.code, p.name_nep, d.code, d.name, pr.pension_type_code, pt.type_name_nep
        )
        select i.*,
        coalesce((select lagat_katta_count from lagat_katta_info
          where province_code = i.province_code and district_code = i.district_code and pension_type_code = i.pension_type_code), 0
        ) as current_month_lagat_katta_count
        from info i
        order by i.province_code, i.district_code, i.pension_type_code
        """;

    var parameters = new MapSqlParameterSource();
    parameters.addValue("currentMonthStart", currentMonthStart);
    parameters.addValue("currentMonthEnd", currentMonthEnd);
    parameters.addValue("provinceCode", provinceCode);
    parameters.addValue("districtCode", districtCode);
    return namedParameterJdbcTemplate.queryForList(sql, parameters);
  }

  public List<Map<String, Object>> getExpenseAssumptionDetails() {
    var sql = """
          with voucher_info as (
            select * from (
              select row_number() over(partition by pension_subpno order by id desc) as rn, id,
              pension_subpno, new_pension_amount
              from voucher_transaction_master
              where voucher_type = 1 and stage = 4 and id > 142812237
            ) where rn = 1
          ),
          average_increments_info as (
            select pension_type_code, avg(total_pensioners)::int4 as average_increments_count
            from (
              select EXTRACT(YEAR FROM pr.created_date) as year, pr.pension_type_code,
              count(1) as total_pensioners
              from pensioner_registration pr
              where
                pr.pension_type_code in ('1', '2', '3', '4')
                and extract(year from pr.created_date)
                  between (extract(year from current_timestamp) - 5) and (extract(year from current_timestamp) - 1)
              group by EXTRACT(YEAR FROM pr.created_date), pr.pension_type_code
            )
            group by pension_type_code
          ),
          info as (
            select
            pr.pension_type_code, pt.type_name_nep,
            count(1)::int4 as current_pensioners_count,
            round((sum(coalesce(vi.new_pension_amount, pr.pension_amount_per_month)) * 1)::numeric, 2)::float8
            as assumed_pension_expense,
            round((sum(coalesce(vi.new_pension_amount, pr.pension_amount_per_month)) * 13)::numeric, 2)::float8
            as assumed_budget
            from pensioner_registration pr
            inner join pensioner_types pt on pt.type_code = pr.pension_type_code
            left join voucher_info vi on vi.pension_subpno = pr.pension_subpno
            where
              pr.pension_type_code in ('1', '2', '3', '4')
              and pr.pension_status in (1,2,3)
              and (pr.main_bank_code is not null and pr.main_bank_code != 0)
              and (pr.pension_bank_code is not null and pr.pension_bank_code != '')
            group by pr.pension_type_code, pt.type_name_nep
          )
          select i.*, coalesce(ai.average_increments_count, 0) as assumed_increments_count
          from info i
          left join average_increments_info ai on ai.pension_type_code = i.pension_type_code
          order by i.pension_type_code
        """;

    return jdbcTemplate.queryForList(sql);
  }

  public List<Map<String, Object>> getTameliCountSummaryDetails(String fy) {
    var currentDate = LocalDate.now();
    var npDate = nepaliCalendarRepository.findByRomanDate(currentDate)
        .orElseThrow(() -> new ResourceNotFoundException("Nepali date not found"));
    var currentMonthStart = String.format("%d-%02d-%02d", npDate.getNepaliYear(), npDate.getNepaliMonth(), 1);
    var currentMonthEnd = String.format("%d-%02d-%02d", npDate.getNepaliYear(), npDate.getNepaliMonth(),
        npDate.getNepaliDaysInMonth());

    var sql = """
        select
          pt.type_name_nep,
          sum(case when vm.fiscal_year < :fy then 1 else 0 end) as total_until_prev_fy,
          sum(case when vm.fiscal_year = :fy and nc.nepali_date < :currentMonthStart then 1 else 0 end) as current_fy_until_prev_month,
          sum(case when nc.nepali_date between :currentMonthStart and :currentMonthEnd then 1 else 0 end) as current_month
          from voucher_transaction_master vm
          inner join nepali_calendar nc on nc.roman_date = vm.created_date::date
          inner join pensioner_registration pr on pr.pension_subpno = vm.pension_subpno
          right join pensioner_types pt on pt.type_code = pr.pension_type_code
          where vm.voucher_type = 1
            and vm.stage = 4
            and vm.calculation_type_code in ('8', '9')
            and pr.pension_type_code in ('1', '2', '3', '4')
          group by pr.pension_type_code, pt.type_name_nep
          order by pr.pension_type_code
        """;

    var parameters = new MapSqlParameterSource();
    parameters.addValue("currentMonthStart", currentMonthStart);
    parameters.addValue("currentMonthEnd", currentMonthEnd);
    parameters.addValue("fy", fy);
    return namedParameterJdbcTemplate.queryForList(sql, parameters);
  }

  public Map<String, Object> getNepaliIncomeCertificateData(ReportDto dto) {
    var currencyRate = nrbService.getTodayRateForCurrency(dto.getCurrencyCode());
    if (currencyRate == 0)
      currencyRate = 1;

    var sql = """
        with voucher_info as (
            select * from (
              select row_number() over(partition by pension_subpno order by id desc) as rn, id,
              pension_subpno, new_pension_amount
              from voucher_transaction_master
              where pension_subpno = :subPNo and voucher_type = 1 and stage = 4 and id > 142812237 and fiscal_year = :fy
            ) where rn = 1
        ),
        user_info as (
          select u.full_name, p.name_nep
          from users u
          left join user_posts p on p.id = u.post_id
          where u.username = :user
        )
        select
        (select nepali_date from nepali_calendar where roman_date = current_date) as date,
        case when pr.pension_gender = '1' then 'श्री ' else 'श्रीमती ' end ||
          case when pr.pension_sub_pensioner = 0 then pr.pension_name_nep
            else pr.pension_sub_name_nep
          end
        as pensioner_name,
        pr.pension_subpno,
        pt.type_name_nep as type_name,
        coalesce(vi.new_pension_amount, pr.pension_amount_per_month) as monthly_pension,
        round((coalesce(vi.new_pension_amount, pr.pension_amount_per_month) / :currencyRate)::numeric, 2)::float8 as monthly_pension_other,
        (select full_name from user_info) as user_fullname,
        (select name_nep from user_info) as user_post
        from pensioner_registration pr
        inner join pensioner_types pt on pt.type_code = pr.pension_type_code
        left join voucher_info vi on vi.pension_subpno = pr.pension_subpno
        where pr.pension_subpno = :subPNo
        """;

    var parameters = new MapSqlParameterSource();
    parameters.addValue("subPNo", dto.getPensionSubPNo());
    parameters.addValue("fy", dto.getFy());
    parameters.addValue("currencyRate", currencyRate);
    parameters.addValue("user", CurrentUser.getUserName());

    var data = namedParameterJdbcTemplate.queryForMap(sql, parameters);
    data.put("currency_rate", currencyRate);
    data.put("currency_code", dto.getCurrencyCode());
    return data;
  }

  public Map<String, Object> getEnglishIncomeCertificateData(ReportDto dto) {
    var currencyRate = nrbService.getTodayRateForCurrency(dto.getCurrencyCode());
    if (currencyRate == 0)
      currencyRate = 1;

    var sql = """
        with voucher_info as (
            select * from (
              select row_number() over(partition by pension_subpno order by id desc) as rn, id,
              pension_subpno, new_pension_amount
              from voucher_transaction_master
              where pension_subpno = :subPNo and voucher_type = 1 and stage = 4 and id > 142812237 and fiscal_year = :fy
            ) where rn = 1
        ),
        user_info as (
          select u.full_name, p.name_eng
          from users u
          left join user_posts p on p.id = u.post_id
          where u.username = :user
        )
        select
        (select to_char(current_date, 'yyyy-mm-dd')) as date,
        case when pr.pension_gender = '1' then 'Mr. ' else 'Ms. ' end ||
          case when pr.pension_sub_pensioner = 0 then pr.pension_name_eng
            else pr.pension_sub_name_eng
          end
        as pensioner_name,
        pr.pension_subpno,
        pt.type_name_eng as type_name,
        coalesce(vi.new_pension_amount, pr.pension_amount_per_month) as monthly_pension,
        round((coalesce(vi.new_pension_amount, pr.pension_amount_per_month) / :currencyRate)::numeric, 2)::float8 as monthly_pension_other,
        (select full_name from user_info) as user_fullname,
        (select name_eng from user_info) as user_post
        from pensioner_registration pr
        inner join pensioner_types pt on pt.type_code = pr.pension_type_code
        left join voucher_info vi on vi.pension_subpno = pr.pension_subpno
        where pr.pension_subpno = :subPNo
        """;

    var parameters = new MapSqlParameterSource();
    parameters.addValue("subPNo", dto.getPensionSubPNo());
    parameters.addValue("fy", dto.getFy());
    parameters.addValue("currencyRate", currencyRate);
    parameters.addValue("user", CurrentUser.getUserName());

    var data = namedParameterJdbcTemplate.queryForMap(sql, parameters);
    data.put("currency_rate", currencyRate);
    data.put("currency_code", dto.getCurrencyCode());
    return data;
  }

  public List<Map<String, Object>> getPensionerSearch(String sl1, String sv1, String sl2, String sv2) {
    var sql = """
        with voucher_info as (
            select * from (
            select row_number() over(partition by pension_subpno order by id desc) as rn, id,
            pension_subpno, new_pension_amount
            from voucher_transaction_master
            where voucher_type = 1 and stage = 4 and id > 142812237
            ) where rn = 1
        ),
        vouchers as (
          select pension_subpno, array_agg(voucher_no) as voucher_numbers
          from voucher_transaction_master
          where voucher_type = 1 and id > 142812237
          group by pension_subpno
        ),
        presence_info as (
            select * from (
                select
                row_number() over(partition by pp.pension_subpno order by pp.present_date_eng desc) as rn,
                pp.pension_subpno, nc.nepali_date
                from pensioner_presence pp
                inner join nepali_calendar nc on nc.roman_date = pp.present_date_eng
            ) where rn = 1
        )
        select
        case when pr.pension_sub_pensioner = 0 then pr.pension_name_nep else pr.pension_sub_name_nep end as name_nep,
        case when pr.pension_sub_pensioner = 0 then pr.pension_name_eng else pr.pension_sub_name_eng end as name_eng,
        case when pr.pension_sub_pensioner = 0 then 'मुख्य' when coalesce(pr.pension_name_nep, '') = '' then 'पारिवारिक' else 'पारिवारिक (सोझै)' end as registration_type,
        pt.type_name_nep,
        pr.pension_subpno,
        pr.national_id,
        coalesce(vi.new_pension_amount, pr.pension_amount_per_month) as monthly_pension,
        pr.pension_retire_nep,
        coalesce(pp.post_name_nep, '') as pension_post,
        pr.pension_phone_no,
        ot.description || ' (' || o.office_name_nep || ')' as pension_bank,
        pr.pension_account_no,
        coalesce(pi.nepali_date, '') as presence_date,
        ps.status_name_nep,
        '' as remarks
        from pensioner_registration pr
        inner join pensioner_types pt on pt.type_code = pr.pension_type_code
        inner join office_types ot on ot.id = pr.main_bank_code
        inner join offices o on o.office_code = pr.pension_bank_code
        inner join pensioner_status ps on ps.id = pr.pension_status
        left join pensioner_posts pp on pp.pensioner_post_code = pr.pension_post_code
        left join voucher_info vi on vi.pension_subpno = pr.pension_subpno
        left join vouchers v on v.pension_subpno = pr.pension_subpno
        left join presence_info pi on pi.pension_subpno = pr.pension_subpno
        where
          case when :sl1 = 'pattaNumber' then pr.pension_subpno = :sv1 else false end
          or
          case when :sl1 = 'sanketNumber' then pr.sanket_no = :sv1 else false end
          or
          case when :sl1 = 'accountNumber' then pr.pension_account_no = :sv1 else false end
          or
          case when :sl1 = 'nidNumber' then pr.national_id = :sv1 else false end
          or
          case when :sl1 = 'mobileNumber' then pr.pension_phone_no = :sv1 else false end
          or
          case when :sl1 = 'retirementDate' then pr.pension_retire_nep between :sv1 and :sv2 else false end
          or
          case when :sl1 = 'voucherNo' then :sv1 = any(v.voucher_numbers) else false end
          or
          case when :sl1 = 'name' then
            (case when pr.pension_sub_pensioner = 0 then lower(pr.pension_name_nep) like lower(:sv1)
            else lower(pr.pension_sub_name_nep) like lower(:sv1) end)
            or
            (case when pr.pension_sub_pensioner = 0 then lower(pr.pension_name_eng) like lower(:sv1)
            else lower(pr.pension_sub_name_eng) like lower(:sv1) end)
          else false end
        order by name_eng
          """;

    var parameters = new MapSqlParameterSource();
    parameters.addValue("sl1", sl1);
    parameters.addValue("sv1", sv1);
    parameters.addValue("sv2", sv2);
    return namedParameterJdbcTemplate.queryForList(sql, parameters);
  }
}
