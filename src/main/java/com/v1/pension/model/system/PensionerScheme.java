package com.v1.pension.model.system;

import com.v1.pension.model.BaseModel;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "pensioner_schemes")
@Data
@EqualsAndHashCode(callSuper = true)
public class PensionerScheme extends BaseModel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String typeCode;

  @Column(unique = true, nullable = false)
  private String schemeCode;

  private String schemeNameNep;

  private String schemeNameEng;

  private int showType; // 0 for all and 1 to Main, 2 for sub pensioner, 11 for direct

  private boolean isRequiredEndDate; //0 for not required and 1 for required

  @ColumnDefault("0")
  private double incrementPercent;

  private int status;
}