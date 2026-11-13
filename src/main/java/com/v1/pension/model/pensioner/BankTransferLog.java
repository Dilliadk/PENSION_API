package com.v1.pension.model.pensioner;

import com.v1.pension.model.BaseModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "bank_transfer_logs")
public class BankTransferLog extends BaseModel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String pensionSubPNo; // foreign key for PensionRegistration
  private String transferDate;

  private int oldMainBankCode;
  private String oldPensionBankCode;

  private int newMainBankCode;
  private String newPensionBankCode;
}
