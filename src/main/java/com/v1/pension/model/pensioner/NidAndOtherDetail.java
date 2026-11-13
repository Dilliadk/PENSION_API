package com.v1.pension.model.pensioner;

import com.v1.pension.model.BaseModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "nid_and_other_details")
public class NidAndOtherDetail extends BaseModel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String pensionSubPNo; // foreign key for PensionRegistration

  private String registrationDate;

  private String otherTypeCode;

  private String nid;

  private String pan;

  private String other;

  private int status = 1;

  private int stage = 1;
}
