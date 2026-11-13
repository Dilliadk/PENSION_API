package com.v1.pension.model.system;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "menu_button")
public class MenuButton {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "menu_sub_id")
  private MenuSub menuSub;

  @Column(name = "menu_button_name_nep", nullable = false)
  private String menuButtonNameNep;

  @Column(name = "menu_button_name_eng", nullable = false)
  private String menuButtonNameEng;

  @Column(name = "menu_button_icon")
  private String menuButtonIcon;

  @Column(name = "display_order")
  private Integer displayOrder;

  @Column(name = "menu_url")
  private String menuUrl;
}
