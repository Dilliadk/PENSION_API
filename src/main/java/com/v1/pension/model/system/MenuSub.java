package com.v1.pension.model.system;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "menu_sub")
public class MenuSub {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "menu_main_id")
  private MenuMain menuMain;

  @Column(name = "menu_sub_name_nep", nullable = false)
  private String menuSubNameNep;

  @Column(name = "menu_sub_name_eng", nullable = false)
  private String menuSubNameEng;

  @Column(name = "menu_sub_icon")
  private String menuSubIcon;

  @Column(name = "display_order")
  private Integer displayOrder;
}
