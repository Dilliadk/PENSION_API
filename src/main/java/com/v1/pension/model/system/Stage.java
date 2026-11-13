package com.v1.pension.model.system;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "stages")
public class Stage {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  private int code;

  private String stageNameNep;
  private String stageNameEng;
}
