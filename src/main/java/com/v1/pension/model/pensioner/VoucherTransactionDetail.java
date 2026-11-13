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

@Entity
@Table(name = "voucher_transaction_details")
@Data
@EqualsAndHashCode(callSuper = true)
public class VoucherTransactionDetail extends BaseModel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String pensionSubPNo; // foreign key to pension_registration

  private Long masterId;

  private String fiscalYear;

  private String startDate;

  private String endDate;

  private int period; // pension period like 1, 15 etc

  private int periodType; // 1 = Day, 2 = Month

  private double receivableAmount;

  private double receiveAmount;

  private double difference;

  private double increment;

  @ColumnDefault("0")
  private double tax;

  private double totalAmount;

  private String eftNo;

  @Column(length = 500)
  private String eftRemarks;

  @Column(length = 100)
  private String accountNameNep; // for payment

  @Column(length = 100)
  private String accountNameEng; // for payment

  private Integer bankId; // for payment

  @Column(length = 20)
  private String accountNo; // for payment

  @Column(length = 50)
  private String payeeCode;

  @ColumnDefault("1")
  private int status = 1; // for payment: 1 = new, 2 = processing, 3 = success, 4 = rejected, 5 = reverse

  @Transient
  private int group;
}