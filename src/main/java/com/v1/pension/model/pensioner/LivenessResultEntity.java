package com.v1.pension.model.pensioner;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

@Data
@Entity
@Table(name = "liveness_results")
public class LivenessResultEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @CreationTimestamp
  private LocalDateTime createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pensioner_presence_id")
  private PensionerPresence pensionerPresence;

  private String pensionSubPNo;

  private boolean faceValid;

  private boolean gestureValid;

  private double nidFaceMatchPercentage;

  private int status = 1; // 1 = new/on-going, 2 = processing, 3 = completed
}
