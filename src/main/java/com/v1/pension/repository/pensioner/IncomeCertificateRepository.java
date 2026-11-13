package com.v1.pension.repository.pensioner;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.Tuple;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.v1.pension.model.pensioner.IncomeCertificateEntity;

public interface IncomeCertificateRepository extends JpaRepository<IncomeCertificateEntity, Long> {
  @Query(nativeQuery = true, value = """
      select * from income_certificates
      where pension_subpno = :subPNo
        and fiscal_year = :fy
        and version = :version
        and currency_code = :currencyCode
      """)
  Optional<IncomeCertificateEntity> findCertificate(String subPNo, String fy, String version, String currencyCode);

  @Query(nativeQuery = true, value = """
      select ic.id, ic.created_by, nc.nepali_date || ' | ' || to_char(ic.created_date, 'HH:MI:SS AM') as created_at,
      ic.fiscal_year, ic.currency_code, ic.version
      from income_certificates ic
      inner join nepali_calendar nc on nc.roman_date = ic.created_date::date
      where ic.pension_subpno = :subPNo
      order by ic.id desc
      """)
  List<Tuple> getIncomeCertificatesForPensioner(String subPNo);
}
