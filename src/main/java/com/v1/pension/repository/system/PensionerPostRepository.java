package com.v1.pension.repository.system;

import com.v1.pension.dto.pensioner.pensioner_post.PensionerPostResponseDto;
import com.v1.pension.model.system.PensionerPost;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PensionerPostRepository extends JpaRepository<PensionerPost, Long> {
  @Query(nativeQuery = true, value = """
      select pp.id, pl.level_name_nep, pc.category_nep, pp.pensioner_post_code,
      pp.post_name_nep, pp.post_name_eng, s.status_name_nep
      from pensioner_posts pp
      inner join pensioner_levels pl on pl.pensioner_level_code = pp.pensioner_level_code
      inner join pensioner_categories pc on pc.pensioner_category_code = pp.pensioner_category_code
      inner join status s on s.id = pp.status
      order by pp.pensioner_post_code
      """)
  List<Tuple> getAllPensionerPostDetails();

  @Query(nativeQuery = true, value = """
      select id, pensioner_post_code as code, (pensioner_post_code || ' - ' || post_name_nep) as name
      from pensioner_posts
      where status = 1
        and pensioner_category_code = :pcCode
        and pensioner_level_code = :plCode
      order by pensioner_post_code
      """)
  List<Tuple> getActivePensionerPostSummariesByCategoryAndLevel(String pcCode, String plCode);

  @Query(nativeQuery = true, value = """
      select pp.*, pl.pensioner_type_code
      from pensioner_posts pp
      inner join pensioner_levels pl on pl.pensioner_level_code = pp.pensioner_level_code
      where pp.id = :id
      """)
  Optional<PensionerPostResponseDto> getResponseById(@Param("id") long id);

  boolean existsByPensionerPostCode(String pensionerPostCode);
}