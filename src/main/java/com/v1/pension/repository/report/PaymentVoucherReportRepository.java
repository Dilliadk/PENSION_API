package com.v1.pension.repository.report;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.v1.pension.exception.ResourceNotFoundException;
import com.v1.pension.repository.pensioner.VoucherTransactionMasterRepository;

@Repository
@RequiredArgsConstructor
public class PaymentVoucherReportRepository {
  private final NamedParameterJdbcTemplate jdbcTemplate;
  private final VoucherTransactionMasterRepository masterRepository;

  public Map<String, Object> getMasterData(long masterId) {
    var sql = """
        select vm.fiscal_year, pt.type_name_nep, vm.statement_date,
        case vm.payment_type when 1 then 'Regular' when 2 then 'तामेली' when 3 then 'मानाचामल' end as payment_type
        from voucher_transaction_master vm
        inner join pensioner_types pt on pt.type_code = vm.pensioner_type_code
        where vm.id = :masterId
        """;

    var parameters = new MapSqlParameterSource();
    parameters.addValue("masterId", masterId);
    return jdbcTemplate.queryForMap(sql, parameters);
  }

  public Collection<Map<String, Object>> getDetailsData(long masterId) {
    var vm = masterRepository.findById(masterId)
        .orElseThrow(() -> new ResourceNotFoundException("Payment Voucher Not Found"));

    if (vm.getPaymentType() == 1) {
      return getRegularPaymentDetailsData(masterId);
    } else if (vm.getPaymentType() == 2) {
      return getTameliPaymentDetailsData(masterId);
    } else if (vm.getPaymentType() == 3) {
      return getManachamalPaymentDetailsData(masterId);
    } else {
      return Collections.emptyList();
    }
  }

  private Collection<Map<String, Object>> getTameliPaymentDetailsData(long masterId) {
    var sql = """
          with tameli_information as (
            select * from (
                select row_number() over(partition by vm.pension_subpno order by vm.id desc) as rn,
                p.id as pensioner_id, vm.pension_subpno, vm.nominee, vm.nominee_bank_code, vm.nominee_bank_account_no, b.name as bank_name
                from voucher_transaction_master vm
                inner join banks b on b.code = vm.nominee_bank_code
                inner join pensioner_registration p on p.pension_subpno = vm.pension_subpno
                where vm.voucher_type = 1 and vm.stage = 4 and vm.id > 142820925
                and vm.calculation_type_code in ('8', '9')
              ) where rn = 1
          )
          select ti.pension_subpno, ti.nominee as name_nep, ti.bank_name,
          ti.nominee_bank_account_no as pension_account_no,
          vd.receivable_amount as receivable_amount,
          vd.tax as tax,
          vd.total_amount as total_amount,
          case vd.status when 1 then 'New' when 2 then 'Processing' when 3 then 'Success'
            when 4 then 'Reject (' || vd.eft_remarks || ')'
          end as status
          from voucher_transaction_details vd
          inner join tameli_information ti on ti.pension_subpno = vd.pension_subpno
          where vd.master_id = :masterId
        """;

    var parameters = new MapSqlParameterSource();
    parameters.addValue("masterId", masterId);
    return jdbcTemplate.queryForList(sql, parameters);
  }

  private Collection<Map<String, Object>> getManachamalPaymentDetailsData(long masterId) {
    var sql = """
          with tameli_information as (
          select * from (
                select row_number() over(partition by vm.pension_subpno order by vm.id desc) as rn,
                p.id as pensioner_id, vm.pension_subpno, vm.nominee, vm.nominee_bank_code, vm.nominee_bank_account_no, b.name as bank_name
                from voucher_transaction_master vm
                inner join banks b on b.code = vm.nominee_bank_code
                inner join pensioner_registration p on p.pension_subpno = vm.pension_subpno
                where vm.voucher_type = 1 and vm.stage = 4 and vm.id > 142820925
                and vm.new_scheme_code = '12'
              ) where rn = 1
        )
            select ti.pension_subpno, ti.nominee as name_nep, ti.bank_name,
            ti.nominee_bank_account_no as pension_account_no,
            vd.receivable_amount as receivable_amount,
            vd.tax as tax,
            vd.total_amount as total_amount,
            case vd.status when 1 then 'New' when 2 then 'Processing' when 3 then 'Success'
              when 4 then 'Reject (' || vd.eft_remarks || ')'
            end as status
            from voucher_transaction_details vd
            inner join tameli_information ti on ti.pension_subpno = vd.pension_subpno
            where vd.master_id = :masterId
          """;

    var parameters = new MapSqlParameterSource();
    parameters.addValue("masterId", masterId);
    return jdbcTemplate.queryForList(sql, parameters);
  }

  private Collection<Map<String, Object>> getRegularPaymentDetailsData(long masterId) {
    var sql = """
        select pr.pension_subpno,
        case when pr.pension_sub_pensioner = 0 then pr.pension_name_nep
            else pr.pension_name_nep || 'को ' || r.relation_name_nep || ' ' || pr.pension_sub_name_nep
        end as name_nep,
        ot.name || ' (' || o.office_name_nep || ')' as bank_name,
        pr.pension_account_no,
        vd.receivable_amount as receivable_amount,
        vd.tax as tax,
        vd.total_amount as total_amount,
        case vd.status when 1 then 'New' when 2 then 'Processing' when 3 then 'Success'
          when 4 then 'Reject (' || vd.eft_remarks || ')'
        end as status
        from voucher_transaction_details vd
        inner join pensioner_registration pr on pr.pension_subpno = vd.pension_subpno
        inner join offices o on o.office_code = pr.pension_bank_code
        inner join office_types ot on ot.id = pr.main_bank_code
        left join pensioner_relation r on r.relation_code = pr.pension_relation
        where vd.master_id = :masterId
        order by vd.receivable_amount
        """;

    var parameters = new MapSqlParameterSource();
    parameters.addValue("masterId", masterId);
    return jdbcTemplate.queryForList(sql, parameters);
  }
}
