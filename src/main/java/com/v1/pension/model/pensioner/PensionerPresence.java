package com.v1.pension.model.pensioner;

import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.springframework.web.multipart.MultipartFile;

import com.v1.pension.model.BaseModel;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "pensioner_presence")
public class PensionerPresence extends BaseModel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String pensionSubPNo; // foreign key for PensionRegistration

  private String presentDate;

  @Column(columnDefinition = "date")
  private LocalDate presentDateEng;

  private String nid;

  @Column(name = "file_data", columnDefinition = "bytea")
  private byte[] fileData;

  @Column(length = 100)
  private String contentType;

  @Column(columnDefinition = "text")
  private String remarks;

  private int status = 1;

  private int stage = 1;

  private Integer type; // 1 = physical-presence (Biometric), 2 = virtual-presence, 3 = manual-presence

  @Transient
  private MultipartFile file;
}
