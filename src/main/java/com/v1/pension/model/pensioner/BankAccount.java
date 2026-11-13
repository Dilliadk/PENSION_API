package com.v1.pension.model.pensioner;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.v1.pension.model.BaseModel;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "bank_accounts")
public class BankAccount extends BaseModel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String pensionSubPNo; // foreign key for PensionRegistration

  private String registrationDate;

  private String accountNo;

  @Column(length = 200)
  private String accountName;

  private int status = 1;

  private int stage = 1;
}
