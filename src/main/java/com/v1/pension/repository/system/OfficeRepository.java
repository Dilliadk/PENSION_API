package com.v1.pension.repository.system;

import com.v1.pension.dto.system.office.OfficeResponseDto;
import com.v1.pension.model.system.Office;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OfficeRepository extends JpaRepository<Office, Long> {
  boolean existsByOfficeCode(String officeCode);

  Optional<Office> findByOfficeCode(String officeCode);

  @Query(nativeQuery = true, value = """
      select o.id, ot.name as office_type_name,
      o.office_code, o.office_name_nep, o.office_name_eng,
      d.name as district_name, o.remarks, s.status_name_nep as status
      from offices o
      inner join districts d on d.code = o.district_code
      inner join office_types ot on ot.id = o.office_type_id
      inner join status s on s.id = o.status
      order by o.office_code
      """)
  List<Tuple> getAllOfficeDetails();

  @Query(nativeQuery = true, value = """
      select id, office_code as code,
      office_code || ' - ' || office_name_nep as name
      from offices
      where (:typeId = -1 or office_type_id = :typeId) and status = 1
      order by office_code
      """)
  List<Tuple> getActiveOfficeSummaries(@Param("typeId") int typeId);

  @Query(nativeQuery = true, value = "select * from offices where id = :id")
  Optional<OfficeResponseDto> getOfficeResponseById(@Param("id") long id);

  @Query(nativeQuery = true, value = "select district_code from offices where office_code = :officeCode")
  String getDistrictCodeForOffice(String officeCode);
}
