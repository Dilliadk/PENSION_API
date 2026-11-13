package com.v1.pension.repository.system;

import com.v1.pension.model.system.PensionAgeBracket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PensionAgeBracketRepository extends JpaRepository<PensionAgeBracket, Long> {
    List<PensionAgeBracket> findByAgeFromLessThanEqualAndAgeToGreaterThanEqual(int ageFrom, int ageTo);
    List<PensionAgeBracket> findByLabel(String label);
}