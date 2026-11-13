package com.v1.pension.repository.report;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.v1.pension.exception.BadRequestException;
import com.v1.pension.repository.system.OfficeRepository;
import com.v1.pension.utils.CurrentUser;

@Repository
@RequiredArgsConstructor
public class ReportHeaderInfoRepository {
  private final OfficeRepository officeRepository;
  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  public Map<String, Object> getReportHeaderInfo() {
    var office = officeRepository.findByOfficeCode(CurrentUser.getOfficeCode())
        .orElseThrow(() -> new BadRequestException("JWT Error: Office Not Found"));

    Map<String, Object> data = new HashMap<>();
    data.put("office_name", office.getOfficeNameNep());
    data.put("office_location", office.getAddress());
    data.put("office_name_eng", office.getOfficeNameEng());
    data.put("office_location_eng", office.getAddressEng());

    return data;
  }

  public Map<String, Object> getDigitalSignatureInfoNepali() {
    var sql = """
        select
        'प्रमाणित डिजीटल हस्ताक्षर गर्ने व्यक्ति' as title,
        full_name as name,
        (select nepali_date from nepali_calendar where roman_date = current_date) || ' | ' || to_char(current_timestamp, 'HH:MI:SS AM') as date
        from users
        where username = :uName
        """;

    var parameters = new MapSqlParameterSource();
    parameters.addValue("uName", CurrentUser.getUserName());
    return namedParameterJdbcTemplate.queryForMap(sql, parameters);
  }
}
