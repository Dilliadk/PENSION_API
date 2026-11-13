package com.v1.pension.model.system;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.v1.pension.model.BaseModel;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "offices")
public class Office extends BaseModel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "office_type_id", nullable = false)
  private OfficeType type;

  @Column(nullable = false, unique = true)
  private String officeCode;

  private String officeNameNep;

  private String officeNameEng;

  private String districtCode;

  private String address;

  private String addressEng;

  private String remarks;

  private int status;
}
