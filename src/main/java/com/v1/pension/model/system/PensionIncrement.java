package com.v1.pension.model.system;

import com.v1.pension.model.BaseModel;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "pension_increments")
@Data
@EqualsAndHashCode(callSuper = true)
public class PensionIncrement extends BaseModel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String oldId;

  private String incrementCode;

  @Column(nullable = false)
  private String typeCode;

  @Column(nullable = false)
  private String categoryCode;

  @Column(nullable = false)
  private String levelCode;

  @Column(nullable = false)
  private String postCode;

  @Column(nullable = false)
  private String schemeCode;

  @Column(nullable = false)
  private String incrementDate;

  @Column(nullable = false)
  private Double incrementAmount;

  @Column(nullable = false)
  private int status;
}