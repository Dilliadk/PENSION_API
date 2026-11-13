package com.v1.pension.repository.other;

import lombok.RequiredArgsConstructor;

import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.v1.pension.exception.BadRequestException;

@Repository
@RequiredArgsConstructor
public class IpfmsStatusUpdateRepository {
  private final JdbcTemplate jdbcTemplate;
  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  public long saveResult(Long masterId, String payload) {
    try {
      var sql = "insert into ulg_ipfms_status_check_results(master_id, payload) values(?, ?::jsonb) returning id";

      var jsonObj = new PGobject();
      jsonObj.setType("jsonb");
      jsonObj.setValue(payload);

      var id = jdbcTemplate.queryForObject(sql, Long.class, masterId, jsonObj);
      if (id == null)
        throw new BadRequestException("Failed to save result");

      return id;
    } catch (Exception e) {
      throw new BadRequestException(e.getMessage());
    }
  }

  public void deleteResult(long resultId) {
    var sql = "delete from ulg_ipfms_status_check_results where id = :id";
    var parameters = new MapSqlParameterSource();
    parameters.addValue("id", resultId);
    namedParameterJdbcTemplate.update(sql, parameters);
  }

  public void updatePaymentStatus(long resultId, long masterId) {
    var sql = """
        MERGE INTO voucher_transaction_details AS tgt
        USING (
          select t1.*, pr.pension_subpno from (
            SELECT
              (st.payload ->> 'success')::boolean AS is_payment_success,
              s."sno",
              s."payeeCode",
              s."ename",
              s."payableAmount",
              s."eftNo",
              s."status",
              s."rejectReason",
              s."settlementDate"
            FROM ulg_ipfms_status_check_results st,
            jsonb_to_recordset(st.payload -> 'rows')
              AS s(
                "sno" bigint,
                "payeeCode" text,
                "ename" text,
                "payableAmount" numeric,
                "eftNo" text,
                "status" text,
                "rejectReason" text,
                "settlementDate" text
              )
            where st.id = :resultId and st.master_id = :masterId
          ) t1
          inner join pensioner_registration pr on pr.id = t1."payeeCode"::bigint
        ) AS src
        ON tgt.master_id = :masterId and src.is_payment_success = true and tgt.pension_subpno = src.pension_subpno
        WHEN MATCHED THEN
        UPDATE set
          status = case src."status" when 'Success' then 3 when 'Reject' then 4 else tgt.status end,
          eft_no = src."eftNo",
          eft_remarks = src."rejectReason"
          """;

    var parameters = new MapSqlParameterSource();
    parameters.addValue("resultId", resultId);
    parameters.addValue("masterId", masterId);

    namedParameterJdbcTemplate.update(sql, parameters);
  }
}
