package com.v1.pension.model.pensioner;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.envers.Audited;

import com.v1.pension.model.BaseModel;

@Entity
@Table(name = "pensioner_registration")
@Data
@EqualsAndHashCode(callSuper = true)
@Audited
public class PensionerRegistration extends BaseModel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String pensionNo;

  private String dateNep;

  private String pensionTypeCode;

  private String pensionCategoryCode;

  private String pensionPostCode;

  private String pensionLevelCode;

  private String pensionNameNep;

  private String pensionNameEng;

  private String pensionGender;

  private String pensionDobNep;

  private String pensionProvince;

  private String pensionDistrict;

  private String vdc;

  private String pensionAddress;

  @Column(length = 2)
  private String ward;

  private String pensionRetireOffice;

  private String pensionRetireNep;

  private Double pensionAmount;

  private String pensionSchemeCode;

  private String pensionDateNep;

  private String pensionEndDateNep;

  @Column(length = 100)
  private String pensionPhoneNo;

  private String pensionEmail;

  private String sanketNo;

  @Column(length = 100)
  private String panNumber;

  @Column(length = 20)
  private String citizenshipNo;

  @Column(length = 100)
  private String nationalId;

  private Integer mainBankCode;
  private String pensionBankCode; //selected bank branch code
  private String pensionAccountNo; //Update after bank account open

  @Column(nullable = false)
  private Double pensionAmountPerMonth; //starting Pension define by kitab khana

  private String pensionPaidUpTo; //update at the time of EFT
  private String pensionLastSetDate; // update at the time of Increment or Last Voucher Calculation
  private Double pensionLastSetAmount; // update at the time of Increment or Last Voucher Calculation

  private int pensionSubPensioner; // 0 for main-pensioner and 1 for sub-pensioner

  private String pensionDeathDateNep;

  @Column(unique = true, nullable = false)
  private String pensionSubPNo; // p

  private String pensionSubNameNep;
  private String pensionSubNameEng;

  private int festivalMonth;
  private int isIncrement;
  private String presentDate;
  private String year75Date;
  private String year85Date;
  private String year60Date;

  private Double insuranceAmount;
  private String otherTypeCode;
  private double pensionReceivePercentage;
  private double pensionIncrementPercentage;
  private Integer pensionMaritalStatus; // 1 = married, 2 = single
  private int pensionStatus;
  private String pensionRelation;

  @Column(length = 500)
  private String pensionRemarks;

  @Column(length = 1)
  private int nationalIdStatus; //0 for not verify 1 for verify

  @Column(length = 1)
  private int virtualPresenceStatus; // 0 for not and 1 for yes || 0 = Physical, 1 = Virtual

  @ColumnDefault("1")
  private int messageMedium = 1; // 1 = SMS, 2 = Email

  private int stage;

  @Transient
  private boolean mpDead; // determines if main-pensioner is dead while registering sub-pensioner
}