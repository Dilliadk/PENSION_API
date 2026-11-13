package com.v1.pension.model.pensioner;

import com.v1.pension.model.BaseModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pensioner_block_release")
public class PensionerBlockRelease extends BaseModel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String pensionSubPNo;

  private String dateNep;
  private int type; // 1 = रोक्का, 2 = तामेली, 3 = फुकुवा
  private String organization;
  private String remarks;
  private int stage = 1;
}
