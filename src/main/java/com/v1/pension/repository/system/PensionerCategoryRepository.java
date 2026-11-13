package com.v1.pension.repository.system;

import com.v1.pension.model.system.PensionerCategory;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PensionerCategoryRepository extends JpaRepository<PensionerCategory, Long> {
  @Query(nativeQuery = true, value = """
      select id, pensioner_category_code as code, (pensioner_category_code || ' - ' || category_nep) as name
      from pensioner_categories
      where status = 1 and pensioner_type_code = :ptCode
      order by pensioner_category_code
      """)
  List<Tuple> getActivePensionerCategoriesByType(@Param("ptCode") String ptCode);

  @Query(nativeQuery = true, value = """
      select pc.id, pensioner_category_code, category_nep, category_eng, pt.type_name_nep,
      s.status_name_nep as status
      from pensioner_categories pc
      inner join pensioner_types pt on pt.type_code = pc.pensioner_type_code
      inner join status s on s.id = pc.status
      order by pensioner_category_code
      """)
  List<Tuple> getAllPensionerCategories();
}