package com.v1.pension.repository.report;

import java.util.Collection;
import java.util.Map;
import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MonthlyVoucherReportRepository {
  private final NamedParameterJdbcTemplate jdbcTemplate;

  public Map<String, Object> getMasterData(long masterId) {
    var sql = """
        select fiscal_year, nepali_month_name(calculation_from_date) as month_name, statement_date
        from voucher_transaction_master
        where id = :masterId
        """;

    var parameters = new MapSqlParameterSource();
    parameters.addValue("masterId", masterId);
    return jdbcTemplate.queryForMap(sql, parameters);
  }

  public Collection<Map<String, Object>> getDetailsData(long masterId) {
    var sql = """
        select pr.pension_subpno,
        case when pr.pension_sub_pensioner = 0 then pr.pension_name_nep
            else pr.pension_name_nep || 'को ' || r.relation_name_nep || ' ' || pr.pension_sub_name_nep
        end as name_nep,
        ot.name || ' (' || o.office_name_nep || ')' as bank_name,
        pr.pension_account_no,
        vd.receivable_amount, vd.tax, vd.total_amount
        from voucher_transaction_details vd
        inner join pensioner_registration pr on pr.pension_subpno = vd.pension_subpno
        inner join offices o on o.office_code = pr.pension_bank_code
        inner join office_types ot on ot.id = pr.main_bank_code
        left join pensioner_relation r on r.relation_code = pr.pension_relation
        where vd.master_id = :masterId
        order by vd.receivable_amount desc
          """;

    var parameters = new MapSqlParameterSource();
    parameters.addValue("masterId", masterId);
    return jdbcTemplate.queryForList(sql, parameters);
  }
}
