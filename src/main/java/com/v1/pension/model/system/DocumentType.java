package com.v1.pension.model.system;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "document_type")
public class DocumentType {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String documentTypeNameEng;

  private String documentTypeNameNep;

  private Integer status;
}
