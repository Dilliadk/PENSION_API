package com.v1.pension.model.system;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "pensioner_relation")
public class PensionerRelation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String relationCode;

  private String relationNameNep;

  private String relationNameEng;

  private int status;
}

