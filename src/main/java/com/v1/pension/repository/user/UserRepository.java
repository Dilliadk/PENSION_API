package com.v1.pension.repository.user;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.Tuple;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.v1.pension.dto.auth.RoleResponseDto;
import com.v1.pension.dto.user.UserResponseDto;
import com.v1.pension.model.user.User;

public interface UserRepository extends JpaRepository<User, Long> {
  @Query(nativeQuery = true, value = """
      select u.id, u.username, u.full_name, u.mobile_no,
      r.role_name_eng, o.office_name_nep, s.status_name_nep
      from user_role_mapping m
      inner join users u on u.id = m.user_id and m.status = 1
      inner join offices o on o.office_code = m.office_code
      inner join user_roles r on r.id = m.role_id
      inner join status s on s.id = u.status
      where :officeType = -1 or o.office_type_id = :officeType
      order by u.full_name
        """)
  List<Tuple> getAllUserDetails(int officeType);

  @Query(nativeQuery = true, value = """
      select u.username, u.full_name as name, r.role_name_eng as role, o.office_name_nep as office,
      :fy as fy, current_date as "currentDateEng",
      (select nepali_date from nepali_calendar where roman_date = current_date) as "currentDateNep"
      from user_role_mapping m
      inner join users u on u.id = m.user_id
      inner join offices o on o.office_code = m.office_code
      inner join user_roles r on r.id = m.role_id
      where m.user_id = :userId and m.office_code = :officeCode and r.role_name_eng = :role
      """)
  Tuple getUserDetails(long userId, String officeCode, String role, String fy);

  @Query(nativeQuery = true, value = """
      select distinct u.id, u.full_name || ' (' || o.office_name_nep || ')' as name
      from users u
      inner join user_role_mapping urm on urm.user_id = u.id
      inner join offices o on o.office_code = u.office_code
      where u.status = 1 and urm.status = 1 and urm.role_id = :roleId and urm.office_code = :officeCode
      """)
  List<Tuple> getActiveUserByRoleForOffice(int roleId, String officeCode);

  @Query(nativeQuery = true, value = "select * from users where username = :userName")
  Optional<User> getUserByUsername(@Param("userName") String userName);

  @Query(nativeQuery = true, value = "select * from users where id = :id")
  Optional<UserResponseDto> getUserResponseById(@Param("id") long id);

  @Query(nativeQuery = true, value = """
      select row_number() over() as id, r.id as role_id, r.role_name_nep, r.role_name_eng,
      m.office_code, o.office_name_nep
      from user_role_mapping m
      inner join user_roles r on r.id = m.role_id
      inner join offices o on o.office_code = m.office_code
      where m.user_id = :userId and m.status = 1
      """)
  List<RoleResponseDto> getUserRoles(@Param("userId") long userId);

  @Query(nativeQuery = true, value = """
      select m.role_id as "roleId", m.user_id as "userId", m.office_code as "officeCode",
      r.role_name_nep as "roleNameNep", r.role_name_eng as "roleNameEng",
      r.user_group_id as "userGroupId", ug.group_name_nep as "groupNameNep",
      o.office_name_nep as "officeNameNep", m.status, s.status_name_nep as "statusNameNep"
      from user_role_mapping m
      inner join offices o on o.office_code = m.office_code
      inner join user_roles r on r.id = m.role_id
      inner join user_groups ug on ug.id = r.user_group_id
      inner join status s on s.id = m.status
      where m.user_id = :userId and m.status = 1
      """)
  List<Tuple> getUserRolesDetails(@Param("userId") long userId);

  boolean existsByUsername(String username);

  @Query(nativeQuery = true, value = """
      select
      (select count(1) from voucher_transaction_master m
      left join digitally_signed_documents dsd
        on type = 'letter' and table_name = 'voucher_transaction_master' and record_id = m.id
        where
          (m.stage = 1 and :role = 'PMO_CREATOR') or
          (m.stage = 12 and :role = 'PMO_CREATOR') or --DTCO-Approved to PMO_CREATOR
          (m.stage = 3 and :role = 'PMO_APPROVER') or
          (m.stage = 9 and :role = 'DTCO_CREATOR' and m.office_code = :officeCode) or
          (m.stage = 11 and :role = 'DTCO_APPROVER' and m.office_code = :officeCode) or
          (:role = 'PMO_APPROVER'
          and m.id > 142826736
          and m.voucher_type = 1
          and m.stage = 4
          and m.calculation_type_code in ('1', '2', '4', '5', '8', '9')
          and m.office_code = '00001'
          and dsd.id is null
        )
      ) as pending_tasks_count,
      (select count(1) from bank_to_pmo_notices n where n.status = 1) as bank_notice_count
        """)
  Tuple getDashboardTaskInfo(String role, String officeCode);

  // @Query(nativeQuery = true, value = """
  //     select
  //     (select count(1) from voucher_transaction_master m
  //       where
  //         (m.stage = 1 and :role = 'PMO_CREATOR') or
  //         (m.stage = 12 and :role = 'PMO_CREATOR') or --DTCO-Approved to PMO_CREATOR
  //         (m.stage = 3 and :role = 'PMO_APPROVER') or
  //         (m.stage = 9 and :role = 'DTCO_CREATOR' and m.office_code = :officeCode) or
  //         (m.stage = 11 and :role = 'DTCO_APPROVER' and m.office_code = :officeCode)
  //     ) as pending_tasks_count,
  //     (select count(1) from bank_to_pmo_notices n where n.status = 1) as bank_notice_count
  //       """)
  // Tuple getDashboardTaskInfo(String role, String officeCode);

  @Query(nativeQuery = true, value = """
      with data as (
        select pt.type_name_eng as name, count(p.id) as value
        from pensioner_registration p
        right join pensioner_types pt on pt.type_code = p.pension_type_code
        group by pt.type_code, pt.type_name_eng
        order by count(p.id) desc
      ),
      total_data as (select sum(value) as total_pensioners from data)
      select d.name, d.value, round((d.value / td.total_pensioners * 100), 4) as percent
      from data d
      cross join total_data td
      """)
  List<Tuple> getDashboardCategoryBreakdown();

  @Query(nativeQuery = true, value = """
      --select 'NP0' || pv.code as code, count(p.id) as value, pv.name_eng as display
      select pv.code as code, count(p.id) as value, pv.name_eng as display
      from pensioner_registration p
      right join provinces pv on pv.code = p.pension_province
      group by pv.code, pv.name_eng
      order by pv.code
      """)
  List<Tuple> getDashboardProvinceData();

  @Query(nativeQuery = true, value = """
      with data as (
        select 1 as id, 'Total Pensioners' as title, count(1) as value, 'groups' as icon
        from pensioner_registration where pension_status in (1,2)

        union

        select 2 AS id, 'Total Payment (FY)' AS title, sum(total_amount) as value, 'payments' as icon
        from mv_dashboard_payments
        where voucher_type = 4 and stage = 4 and status in (2,3) and fiscal_year = :fiscalYear
      )
      select * from data order by id
      """)
  List<Tuple> getDashboardKpiData(String fiscalYear);

  @Query(nativeQuery = true, value = """
      with data as (
        select p.pension_status, p.pension_sub_pensioner, count(1) as value
        from pensioner_registration p
        group by p.pension_status, p.pension_sub_pensioner
      ),
      total_data as (
        select
        (select sum(value) from data where pension_status in (1,2)) as total_pensioners,
        (select sum(value) from data where pension_status = 1) as active_pensioners
      ),
      output_data as(
      select
      1 as id,
      'Active Pensioners' as title,
      sum(d.value) as value,
      round((sum(d.value)/td.total_pensioners * 100), 2) || '% of Total' as sub,
      'border-success' as border_color,
      '#00bfa5' as chart_color
      from data d, total_data td where d.pension_status = 1
      group by td.total_pensioners, td.active_pensioners

      union

      select
      2 as id,
      'Main Pensioners' as title,
      sum(d.value) as value,
      round((sum(d.value)/td.active_pensioners * 100), 2) || '% of Active' as sub,
      'border-primary' as border_color,
      '#1976d2' as chart_color
      from data d, total_data td where d.pension_status = 1 and d.pension_sub_pensioner = 0
      group by td.total_pensioners, td.active_pensioners

      union

      select
      3 as id,
      'Sub Pensioners' as title,
      sum(d.value) as value,
      round((sum(d.value)/td.active_pensioners * 100), 2) || '% of Active' as sub,
      'border-purple' as border_color,
      '#7c4dff' as chart_color
      from data d, total_data td where d.pension_status = 1 and d.pension_sub_pensioner = 1
      group by td.total_pensioners, td.active_pensioners

      union

      select
      4 as id,
      'Inactive Pensioners' as title,
      sum(d.value) as value,
      round((sum(d.value)/td.total_pensioners * 100), 2) || '% of Total' as sub,
      'border-secondary' as border_color,
      '#9e9e9e' as chart_color
      from data d, total_data td where d.pension_status = 2
      group by td.total_pensioners, td.active_pensioners
      )
      select * from output_data order by id
      """)
  List<Tuple> getDashboardPensionerData();
}
