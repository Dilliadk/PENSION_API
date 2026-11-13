package com.v1.pension.model.system;

import com.v1.pension.model.BaseModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "bank_to_pmo_notices")
public class BankToPmoNotice extends BaseModel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String noticeDate;
  private String bankBranchCode;
  private String pensionSubPNo;
  private String description;
  private int status = 1; // 1 = new/unresolved, 2 = resolved
}
