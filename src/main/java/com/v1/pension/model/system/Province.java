package com.v1.pension.model.system;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "provinces")
public class Province {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false, length = 10)
  private String code;

  @Column(nullable = false, length = 100)
  private String nameNep;

  @Column(length = 100)
  private String nameEng;
}
