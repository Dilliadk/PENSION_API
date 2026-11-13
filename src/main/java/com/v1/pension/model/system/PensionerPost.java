package com.v1.pension.model.system;

import com.v1.pension.model.BaseModel;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "pensioner_posts")
@Data
@EqualsAndHashCode(callSuper = true)
public class PensionerPost extends BaseModel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String pensionerCategoryCode;

  @Column(nullable = false)
  private String pensionerLevelCode;

  @Column(unique = true, nullable = false)
  private String pensionerPostCode;

  private String postNameNep;
  private String postNameEng;
  private int status;
}