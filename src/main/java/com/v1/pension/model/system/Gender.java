package com.v1.pension.model.system;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "genders")
public class Gender {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  private String genderCode;
  private String genderNameNep;
  private String genderNameEng;
}
