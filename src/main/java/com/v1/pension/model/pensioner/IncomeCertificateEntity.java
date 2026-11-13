package com.v1.pension.model.pensioner;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.v1.pension.model.BaseModel;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "income_certificates", uniqueConstraints = @UniqueConstraint(columnNames = { "pension_subpno",
    "fiscal_year", "version", "currency_code" }))
public class IncomeCertificateEntity extends BaseModel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  private String pensionSubPNo;

  private String fiscalYear;

  private String version;

  private String currencyCode;

  @Column(columnDefinition = "text")
  private String data;
}
