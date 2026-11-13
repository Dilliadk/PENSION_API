package com.v1.pension.repository.pensioner;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.v1.pension.model.pensioner.LivenessResultEntity;

public interface LivenessResultRepository extends JpaRepository<LivenessResultEntity, UUID> {

}
