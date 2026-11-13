package com.v1.pension.model.system;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "pensioner_categories")
@Data
public class PensionerCategory {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String pensionerTypeCode;

  @Column(nullable = false)
  private String pensionerCategoryCode;

  @Column(nullable = false)
  private String categoryNep;

  @Column(nullable = false)
  private String categoryEng;

  private int status;
}