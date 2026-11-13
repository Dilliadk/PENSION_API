package com.v1.pension.model.system;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "office_types")
public class OfficeType {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  private String name;

  private String description;

  private Integer bankId;

  @Column(name = "is_bank")
  @ColumnDefault("true")
  private boolean bank = true;
}
