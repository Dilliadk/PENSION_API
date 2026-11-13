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
@Table(name = "forwarded_data_details")
public class ForwardedDataDetails extends BaseModel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "form_type_id", nullable = false)
  private int formTypeId;

  @Column(name = "master_id", nullable = false)
  private Long masterId;

  @Column(name = "source_stage", nullable = false)
  private int sourceStage;

  @Column(name = "receive_stage", nullable = false)
  private int receiveStage;

  @Column(name = "source_user_id", nullable = false)
  private Long sourceUserId;

  @Column(name = "receive_user_id")
  private Long receiveUserId;

  private int action = 0; // default 0: 0 = no action, 1 = forward/approve, 2 = backward

  @Column(name = "action_date")
  private String actionDate;

  private String note;
}
