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

import com.v1.pension.model.BaseModel;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "voucher_transaction_master")
public class VoucherTransactionMaster extends BaseModel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String pensionSubPNo;
  private String schemeCode;

  @Column(length = 200)
  private String voucherNo;

  private int voucherType; // 1 = Adjustment, 2 = Monthly, 3 = Increment, 4 = Payment, 5 = reverse

  @ColumnDefault("0")
  private int paymentType = 0; // 0 = None, 1 = Regular, 2 = Tameli, 3 = Mana-Chamal, 4 = daily

  private String calculationTypeCode; // for adjustment

  private Integer mainBankCode; // office type id

  private String statementDate;

  private String startDate;

  private String endDate;

  private int status = 1; // 1 = new, 2 = sent to ipfms

  private int stage;

  private String fiscalYear;

  @Transient
  private String nextStartDate;

  @Transient
  private double pensionAmount;

  @Transient
  private Integer month;

  // forward/backward fields
  private String note; // forward/backward note
  private Long forwardId;
  private String userIds = "";

  // extra fields
  private String calculationFromDate;
  private String calculationToDate;
  private String newPensionStartDate;
  private String newPensionEndDate;
  private Double newPensionAmount;
  private String year60Date;
  private String year75Date;
  private String year85Date;
  private Double calculationAmount;
  private Double totalAdjustmentAmt;
  private Double totalPaymentAmt;
  private Double tax;
  private Double statementAmount;
  private Double transferToGovtAdj;
  private Double paymentFromBankAc;
  private Double additionalPayAmount;
  private String nominee;
  private String nomineeBankCode;
  private String nomineeBankAccountNo;
  private String nomineeContact;
  private String newSchemeCode;
  private String remarks;

  private String refBatchNo;

  private String pensionerTypeCode;

  @Column(columnDefinition = "text")
  private String particulars; // extra field for migration

  private Double amount; // extra field for migration

  @ColumnDefault("false")
  private boolean legalCase;

  @Column(length = 20)
  private String officeCode; // can be used to filter out office based transactions

  @ColumnDefault("0")
  private double voucherBalance;

  @ColumnDefault("0")
  private double statementBalance;

  @Transient
  private boolean continued;
}
