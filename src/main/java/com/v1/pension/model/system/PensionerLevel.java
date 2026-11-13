package com.v1.pension.model.system;

import com.v1.pension.model.BaseModel;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "pensioner_levels")
@Data
@EqualsAndHashCode(callSuper = true)
public class PensionerLevel extends BaseModel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String pensionerTypeCode;

  @Column(unique = true, nullable = false)
  private String pensionerLevelCode;

  private String levelNameNep;

  private String levelNameEng;

  private int status;
}