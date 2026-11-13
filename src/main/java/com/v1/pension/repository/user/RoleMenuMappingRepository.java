package com.v1.pension.repository.user;

import java.util.List;

import jakarta.persistence.Tuple;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.v1.pension.model.user.RoleMenuMapping;

public interface RoleMenuMappingRepository extends JpaRepository<RoleMenuMapping, Long> {
  @Query(nativeQuery = true, value = """
      select rm.id, :roleId as role_id, mb.id as menu_button_id,
      coalesce(rm.status_id, 0) as status_id, mb.menu_button_name_nep, mb.menu_button_name_eng
      from menu_button mb
      left join role_menu_mapping rm on mb.id = rm.menu_button_id and rm.role_id = :roleId
      where mb.menu_sub_id = :menuSubId
      """)
  List<Tuple> getMenuButtonDetailsForRole(@Param("roleId") long roleId, @Param("menuSubId") long menuSubId);

  @Query(nativeQuery = true, value = """
      with filtered_buttons AS (
          select mb.id as button_id, mb.menu_sub_id, mb.display_order, mb.menu_button_name_eng, mb.menu_button_name_nep, mb.menu_url, mb.menu_button_icon
          from menu_button mb
          join role_menu_mapping rmm on mb.id = rmm.menu_button_id
          where rmm.role_id = :roleId and rmm.status_id = 1
      ),
      sub_menus_agg AS (
          select
              ms.id AS sub_menu_id, ms.menu_main_id, ms.display_order,
              jsonb_build_object(
                  'id', ms.id,
                  'menu_sub_name_nep', ms.menu_sub_name_nep,
                  'menu_sub_name_eng', ms.menu_sub_name_eng,
                  'menu_sub_icon', ms.menu_sub_icon,
                  'button_menus', jsonb_agg(
                      jsonb_build_object(
                          'id', fb.button_id,
                          'menu_button_name_nep', fb.menu_button_name_nep,
                           'menu_button_name_eng', fb.menu_button_name_eng,
                          'menu_url', fb.menu_url,
                          'menu_button_icon', fb.menu_button_icon
                      ) order by fb.display_order
                  )
              ) AS sub_menu_json
          from menu_sub ms
          join filtered_buttons fb on ms.id = fb.menu_sub_id
          group by ms.id, ms.menu_main_id, ms.display_order, ms.menu_sub_name_nep, ms.menu_sub_name_eng, ms.menu_sub_icon
      ),
      main_menus_agg AS (
          select
              mm.id AS main_menu_id,
              mm.display_order,
              jsonb_build_object(
                  'id', mm.id,
                  'menu_main_name_nep', mm.menu_main_name_nep,
                  'menu_main_name_eng', mm.menu_main_name_eng,
                  'menu_main_icon', mm.menu_main_icon,
                  'sub_menus', jsonb_agg(sma.sub_menu_json ORDER BY sma.display_order)
              ) AS main_menu_json
          from menu_main mm
          join sub_menus_agg sma ON mm.id = sma.menu_main_id
          group by mm.id, mm.menu_main_name_nep, mm.menu_main_name_eng, mm.menu_main_icon
      )
      select jsonb_agg(mma.main_menu_json order by mma.display_order) AS role_menu_permissions
      from main_menus_agg mma;
      """)
  String getFullMenuDetailsForRole(@Param("roleId") long roleId);
}
