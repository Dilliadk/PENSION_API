package com.v1.pension.repository.system;

import java.util.List;

import jakarta.persistence.Tuple;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.v1.pension.model.system.MenuSub;

public interface MenuSubRepository extends JpaRepository<MenuSub, Long> {
  @Query(nativeQuery = true, value = """
      select id, menu_main_id, menu_sub_name_eng as name
      from menu_sub where menu_main_id = :menuMainId""")
  List<Tuple> getAllMenuSubByMenuMain(@Param("menuMainId") long menuMainId);
}
