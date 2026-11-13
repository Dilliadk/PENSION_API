package com.v1.pension.repository.user;

import java.util.List;

import jakarta.persistence.Tuple;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.v1.pension.model.user.LivenessRegistrationRequest;

public interface LivenessRegistrationRequestRepository extends JpaRepository<LivenessRegistrationRequest, Long> {
  @Query(nativeQuery = true, value = """
      select r.id, nc.nepali_date || ' ' || to_char(r.created_at, 'HH:MI:SS AM') as created_date,
      r.mobile_number, r.pension_number, r.national_id,
      case when p.pension_sub_pensioner = 0 then p.pension_name_nep else p.pension_sub_name_nep end as pensioner_name,
      case status when 1 then 'OTP Sent' when 2 then 'Registered' end as status
      from liveness_registration_requests r
      inner join pensioner_registration p on p.pension_subpno = r.pension_number
      inner join nepali_calendar nc on nc.roman_date = r.created_at::date
      order by r.created_at desc
      """)
  List<Tuple> getLivenessRegistrationList();
}
