package com.v1.pension.model.pensioner;


import com.v1.pension.model.BaseModel;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.web.multipart.MultipartFile;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "pensioner_documents")
public class PensionerDocument extends BaseModel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String pensionSubPNo;

  private Long documentTypeId;

  private String documentNo;

  private String description;

  private String fileName;

  private String fileType;

  @Column(length = 100)
  private String contentType;

  @Column(name = "file_data", columnDefinition = "bytea")
  private byte[] fileData;

  @Transient
  private MultipartFile file;
}