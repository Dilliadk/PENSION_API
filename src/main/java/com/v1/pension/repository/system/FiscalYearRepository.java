package com.v1.pension.repository.system;

import java.util.List;

import jakarta.persistence.Tuple;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.v1.pension.model.system.FiscalYear;

public interface FiscalYearRepository extends JpaRepository<FiscalYear, Integer> {
  @Query(nativeQuery = true, value = """
      select id, fiscal_year from fiscal_years
      where status = 1
      order by fiscal_year desc
      """)
  List<Tuple> getActiveFiscalYears();

  @Query(nativeQuery = true, value = """
      select * from fiscal_years
      where status = 1 order by id desc limit 1
      """)
  FiscalYear getCurrentFiscalYear();

  FiscalYear findByFiscalYear(String fiscalYear);
}
