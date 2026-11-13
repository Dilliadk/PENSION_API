package com.v1.pension.repository.report;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.v1.pension.utils.MyUtils;

@Repository
@RequiredArgsConstructor
public class AnusuchiReportRepository {
  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  private static String generalSql = """
        select vm.statement_date as created_date, pr.pension_subpno,
        case when pr.pension_sub_pensioner = 0 then pr.pension_name_nep
        else pr.pension_name_nep || 'को ' || r.relation_name_nep || ' ' || pr.pension_sub_name_nep
        end as pensioner_name,
        --case when pr.pension_sub_pensioner = 1 then '' when pr.pension_gender = '1' then 'श्री' else 'श्रीमती' end as gender_title,
        'श्री' as gender_title,
        ot.description as main_bank_name, o.office_name_nep as bank_branch_name,
        pr.pension_account_no as account_no, vm.transfer_to_govt_adj, vm.fiscal_year, vm.voucher_no,
        co.office_name_nep as creator_office, co.address as creator_address
        from voucher_transaction_master vm
        inner join offices co on co.office_code = vm.office_code
        inner join pensioner_registration pr on pr.pension_subpno = vm.pension_subpno
        inner join offices o on o.office_code = pr.pension_bank_code
        inner join office_types ot on ot.id = pr.main_bank_code
        left join pensioner_relation r on r.relation_code = pr.pension_relation
        where vm.id = :id
      """;

  public Map<String, Object> getAnusuchi1MasterData(long id, String calculationTypeCode) {
    var sql = """
        select vm.statement_date as created_date, pr.pension_subpno, pr.pension_sub_pensioner,
        case when :calculationTypeCode in ('1', '5') then 'निवृत्तभरण भुक्तानी खाता खोलिदिने बारे ।'
        else 'पारिवारिक निवृत्तभरण भुक्तानी खाता खोलिदिने बारे ।'
        end as subject,
        case when pr.pension_sub_pensioner = 0 then pr.pension_name_nep
        else pr.pension_name_nep || 'को ' || r.relation_name_nep || ' ' || pr.pension_sub_name_nep
        end as pensioner_name,
        case when pr.pension_sub_pensioner = 1 then '' when pr.pension_gender = '1' then 'श्री' else 'श्रीमती' end as gender_title,
        ot.description as main_bank_name, o.office_name_nep as bank_branch_name,
        pr2.pension_name_nep as main_name, pr2.pension_death_date_nep as main_death_date,
        pr2.pension_account_no as main_account_no, vm.statement_date, vm.statement_amount,
        vm.transfer_to_govt_adj, vm.fiscal_year, vm.voucher_no,
        co.office_name_nep as creator_office, co.address as creator_address
        from voucher_transaction_master vm
        inner join offices co on co.office_code = vm.office_code
        inner join pensioner_registration pr on pr.pension_subpno = vm.pension_subpno
        inner join offices o on o.office_code = pr.pension_bank_code
        inner join office_types ot on ot.id = pr.main_bank_code
        left join pensioner_registration pr2 on
          (pr2.pension_no = pr.pension_no or pr2.pension_subpno = pr.pension_no) and pr2.pension_sub_pensioner = 0
        left join pensioner_relation r on r.relation_code = pr.pension_relation
        where vm.id = :id
          """;

    var parameters = new MapSqlParameterSource();
    parameters.addValue("id", id);
    parameters.addValue("calculationTypeCode", calculationTypeCode);
    var data = namedParameterJdbcTemplate.queryForMap(sql, parameters);

    Map<String, Object> output = new HashMap<>();
    output.put("created_date", data.get("created_date"));
    output.put("subject", data.get("subject"));

    var part1Body = MessageFormat.format(
        """
            श्री {0}
            {1}
            निवृत्तभरण अधिकार पत्र नं. {2} का श्री {3}को नाममा त्यस बैङ्कमा निवृत्तभरण भुक्तानी खाता खोली खाताको विवरण अद्यावधिक गरी दिनु हुन अनुरोध छ।
            """,
        data.get("main_bank_name"),
        data.get("bank_branch_name"),
        data.get("pension_subpno"),
        data.get("pensioner_name"));

    var part2Body = MessageFormat.format(
        """
            क. श्री {0}को मिति {1} मा मृत्यु भएकोले निजको खाता नं. {2} बन्द गरी दिनुहुन।
            ख. बैङ्क स्टेटमन्ट अनुसार खातामा रकम जम्मा भएको बर्ष र महिना : {3}
            ग. बैङ्क स्टेटमन्ट अनुसार खातामा मौज्दात रकम रु. : {4}
            घ. श्री {0}को खाता नं. {2} बाट सरकारी पेन्सन समायोजन खातामा जम्मा गर्नु पर्ने रकम : रु. {5}
            छ. श्री {0}को खाता नं. {2} मा रु. {5} मौज्दात नभएमा पारिवारिक निवृत्तभरण खाता नखोलि कोष तथा लेखा नियन्त्रक कार्यालय वा निवृत्तभरण व्यवस्थापन कार्यालयमा स्टेटमेन्ट सहित सम्पर्कको लागि पठाइ दिनु हुन ।
            """,
        data.get("main_name"),
        data.get("main_death_date"),
        data.get("main_account_no"),
        data.get("statement_date"),
        data.get("statement_amount"),
        data.get("transfer_to_govt_adj"));

    var body = List.of("1", "5").contains(calculationTypeCode) ? part1Body : part1Body + "\n" + part2Body;
    output.put("body", body);
    output.put("fiscal_year", data.get("fiscal_year"));
    output.put("voucher_no", data.get("voucher_no"));
    output.put("creator_office", data.get("creator_office"));
    output.put("creator_address", data.get("creator_address"));

    return output;
  }

  public Map<String, Object> getAnusuchi4MasterData(long id) {
    var parameters = new MapSqlParameterSource();
    parameters.addValue("id", id);
    var data = namedParameterJdbcTemplate.queryForMap(generalSql, parameters);

    var body = MessageFormat.format(
        """
            श्री {0}
            {1}
            निवृत्तभरण अधिकार पत्र नं. {2} का {3} {4} को नाममा त्यस बैङ्कमा रहेको निवृत्तभरण भुक्तानी खाता नं. {5} मा बढी भुक्तानी भएको रकम रू. {6} (अक्षरेपी: {7}) तहाँ बैङ्कमा रहेको सरकारी पेन्सन समायोजन खातामा ट्रान्सफर गरीदिनु हुन अनुरोध छ।
            """,
        data.get("main_bank_name"),
        data.get("bank_branch_name"),
        data.get("pension_subpno"),
        data.get("gender_title"),
        data.get("pensioner_name"),
        data.get("account_no"),
        data.get("transfer_to_govt_adj"),
        MyUtils.amountInNepaliWord((Double) data.get("transfer_to_govt_adj")));

    Map<String, Object> output = new HashMap<>();
    output.put("created_date", data.get("created_date"));
    output.put("body", body);
    output.put("fiscal_year", data.get("fiscal_year"));
    output.put("voucher_no", data.get("voucher_no"));
    output.put("creator_office", data.get("creator_office"));
    output.put("creator_address", data.get("creator_address"));
    return output;
  }

  public Map<String, Object> getAccountCloseMasterData(long id) {
    var parameters = new MapSqlParameterSource();
    parameters.addValue("id", id);
    var data = namedParameterJdbcTemplate.queryForMap(generalSql, parameters);

    var body = MessageFormat.format(
        """
            श्री {0}
            {1}
            निवृत्तभरण अधिकार पत्र नं. {2} का {3} {4} को नाममा त्यस बैङ्कमा रहेको निवृत्तभरण भुक्तानी खाता नं. {5} बन्द गरी सो खातामा रहेको रकम रू. {6} (अक्षरेपी {7}) तहाँ बैङ्कमा रहेको सरकारी पेन्सन समायोजन खातामा ट्रान्सफर गरीदिनु हुन अनुरोध छ।
            """,
        data.get("main_bank_name"),
        data.get("bank_branch_name"),
        data.get("pension_subpno"),
        data.get("gender_title"),
        data.get("pensioner_name"),
        data.get("account_no"),
        data.get("transfer_to_govt_adj"),
        MyUtils.amountInNepaliWord((Double) data.get("transfer_to_govt_adj")));

    Map<String, Object> output = new HashMap<>();
    output.put("created_date", data.get("created_date"));
    output.put("fiscal_year", data.get("fiscal_year"));
    output.put("voucher_no", data.get("voucher_no"));
    output.put("creator_office", data.get("creator_office"));
    output.put("creator_address", data.get("creator_address"));
    output.put("body", body);
    return output;
  }
}
