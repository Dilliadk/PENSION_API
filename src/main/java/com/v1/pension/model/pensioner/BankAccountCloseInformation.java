package com.v1.pension.model.pensioner;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.v1.pension.model.BaseModel;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "bank_account_close_information")
public class BankAccountCloseInformation extends BaseModel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(length = 20, nullable = false)
  private String pensionSubPNo;

  @Column(length = 20)
  private String date;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "voucher_master_id", nullable = false)
  private VoucherTransactionMaster voucherMaster;

  private double amount;

  @Column(length = 20)
  private String fiscalYear;

  @Column(length = 50, nullable = false)
  private String officeCode;

  private int status = 1; // 1 = new, 2 = completed
}
