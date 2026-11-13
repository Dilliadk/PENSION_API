package com.v1.pension.model.system;

import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Data
@Entity
@Table(name = "pensioner_types")
public class PensionerType {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String typeCode;

  private String typeNameNep;

  private String typeNameEng;

  private boolean isOnline;

  private int status;

  @Column(length = 50)
  private String accountCode;

  @Column(length = 50)
  private String economicCode;

  @Column(length = 50)
  private String activityCode;
}