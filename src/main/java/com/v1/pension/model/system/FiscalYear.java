package com.v1.pension.model.system;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "fiscal_years")
public class FiscalYear {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(unique = true)
  private String fiscalYear;

  private Integer startYear;
  private Integer endYear;

  private int status = 1;
}
