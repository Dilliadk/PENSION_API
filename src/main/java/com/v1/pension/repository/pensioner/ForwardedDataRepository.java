package com.v1.pension.repository.pensioner;

import com.v1.pension.model.pensioner.ForwardedDataDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForwardedDataRepository extends JpaRepository<ForwardedDataDetails, Long> {
}
