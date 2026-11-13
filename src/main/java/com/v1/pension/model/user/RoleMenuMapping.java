package com.v1.pension.model.user;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.v1.pension.model.BaseModel;
import com.v1.pension.model.system.MenuButton;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "role_menu_mapping")
public class RoleMenuMapping extends BaseModel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "role_id", nullable = false)
  private UserRole role;

  @ManyToOne(fetch = FetchType.LAZY)
  @JsonBackReference
  @JoinColumn(name = "menu_button_id", insertable = false, updatable = false)
  private MenuButton menuButton;

  @Column(name = "menu_button_id", nullable = false)
  private Long menuButtonId;

  @Column(name = "remark")
  private String remark;

  @Column(name = "status_id", nullable = false)
  private Integer statusId;
}
