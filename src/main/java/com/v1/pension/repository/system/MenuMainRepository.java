package com.v1.pension.repository.system;

import java.util.List;

import jakarta.persistence.Tuple;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.v1.pension.model.system.MenuMain;

public interface MenuMainRepository extends JpaRepository<MenuMain, Long> {
  @Query(nativeQuery = true, value = "select id, menu_main_name_eng as name from menu_main")
  List<Tuple> getAllMenuMain();
}
