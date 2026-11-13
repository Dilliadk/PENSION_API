package com.v1.pension.repository.system;

import java.util.List;

import jakarta.persistence.Tuple;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.v1.pension.model.system.PensionIncrement;

public interface PensionIncrementRepository extends JpaRepository<PensionIncrement, Long> {
  List<PensionIncrement> findAllBySchemeCodeAndPostCode(String schemeCode, String postCode);

  @Query(nativeQuery = true, value = """
      select pi.id, pt.type_name_nep, pc.category_nep, pl.level_name_nep,
      pp.post_name_nep, ps.scheme_name_nep, s.status_name_nep,
      pi.increment_code, pi.increment_date, pi.increment_amount
      from pension_increments pi
      left join pensioner_types pt on pt.type_code = pi.type_code
      left join pensioner_categories pc on pc.pensioner_category_code = pi.category_code
      left join pensioner_levels pl on pl.pensioner_level_code = pi.level_code
      left join pensioner_posts pp on pp.pensioner_post_code = pi.post_code
      left join pensioner_schemes ps on ps.scheme_code = pi.scheme_code
      left join status s on s.id = pi.status
      where pi.post_code = :postCode
      order by pi.increment_date desc
      """)
  List<Tuple> getAllPensionIncrementByPost(String postCode);
}