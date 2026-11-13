package com.v1.pension.repository.system;

import com.v1.pension.model.system.PensionTaxRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PensionTaxRateRepository extends JpaRepository<PensionTaxRate, Long> {
    Optional<PensionTaxRate> findByYear(int year);
}