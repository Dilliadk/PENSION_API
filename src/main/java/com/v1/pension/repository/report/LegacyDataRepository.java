package com.v1.pension.repository.report;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.v1.pension.exception.BadRequestException;

@Repository
public class LegacyDataRepository {
  private NamedParameterJdbcTemplate dataConnection;
  private NamedParameterJdbcTemplate imageConnection;

  public LegacyDataRepository(
      @Qualifier("legacyDataDataSource") DataSource ds1,
      @Qualifier("legacyImageDataSource") DataSource ds2) {
    this.dataConnection = new NamedParameterJdbcTemplate(ds1);
    this.imageConnection = new NamedParameterJdbcTemplate(ds2);
  }

  public List<Map<String, Object>> getBankUploadData(String pNo) {
    var sql = """
        select * from (
        select
        TranID as tran_id,
        TranDate as tran_date,
        Particulars as particulars,
        PmtYear as pmt_year,
        PmtMonth as pmt_month,
        bank_code as bank_code,
        StartDate as start_date,
        EndDate as end_date,
        Amount as amount,
        Tax as tax,
        govadjustment as gov_adjustment,
        MainTosub_Amt as main_to_sub_amt,
        case when StatementDate <> '0' then StatementDate else '' end as statement_date,
        StatementAmount as statement_amount
        from Pension_Bank_Upload
        where p_no = :pNo

        union all

        select
        TranID as tran_id,
        TranDate as tran_date,
        Particulars as particulars,
        PmtYear as pmt_year,
        PmtMonth as pmt_month,
        bank_code as bank_code,
        StartDate as start_date,
        EndDate as end_date,
        Amount as amount,
        Tax as tax,
        govadjustment as gov_adjustment,
        MainTosub_Amt as main_to_sub_amt,
        case when StatementDate <> '0' then StatementDate else '' end as statement_date,
        StatementAmount as statement_amount
        from Pension_Bulk_Bank_Upload
        where p_no = :pNo
        ) t
        order by 4,5 asc
              """;

    var parameters = new MapSqlParameterSource();
    parameters.addValue("pNo", pNo);
    return dataConnection.queryForList(sql, parameters);
  }

  public List<Map<String, Object>> getLegacyStatementData(String pNo) {
    var sql = """
        select
        t.TranID as tran_id,
        t.TranDate as tran_date,
        t.Particulars,
        t.PmtYear as pmt_year,
        t.PmtMonth as pmt_month,
        t.StartDate as start_date,
        t.EndDate as end_date,
        case when t.Amount < 0 then -t.Amount else 0 end as paid,
        case when t.Amount > 0 then t.Amount else 0 end as payable,
        t.Balance as balance,
        t.Remark as remarks,
        t.postedby as posted_by,
        t.approvedby as approved_by
        from pentrans t
        where t.p_no=:pNo
        order by tran_date, t.TranID
        """;

    var parameters = new MapSqlParameterSource();
    parameters.addValue("pNo", pNo);
    return dataConnection.queryForList(sql, parameters);
  }

  public List<Map<String, Object>> getPensionerDocuments(String subPNo) {
    var sql = """
        select ImageID as id, enter_date, imagetype as type, imagedesc as description
        from pension_image
        where code = :subPNo
        """;

    var parameters = new MapSqlParameterSource();
    parameters.addValue("subPNo", subPNo);
    return imageConnection.queryForList(sql, parameters);
  }

  public Map<String, Object> getPensionerDocumentById(long id) {
    var query = "select ImageID as id, ImageType as type, sign from pension_image where ImageID = :id";
    var parameters = new MapSqlParameterSource();
    parameters.addValue("id", id);

    var res = imageConnection.queryForList(query, parameters);

    if (res.isEmpty())
      throw new BadRequestException("Document Not Found");

    return res.get(0);
  }
}
