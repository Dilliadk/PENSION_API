package com.v1.pension.repository.system;

import com.v1.pension.model.system.Gender;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GenderRepository extends JpaRepository<Gender, Integer> {
  @Query(nativeQuery = true, value = "select id, gender_code as code, gender_name_nep as name from genders")
  List<Tuple> getAllGenders();
}
