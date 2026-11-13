package com.v1.pension.model.system;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "menu_main")
public class MenuMain {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "menu_main_name_nep", nullable = false)
  private String menuMainNameNep;

  @Column(name = "menu_main_name_eng", nullable = false)
  private String menuMainNameEng;

  @Column(name = "menu_main_icon")
  private String menuMainIcon;

  @Column(name = "display_order")
  private Integer displayOrder;
}
