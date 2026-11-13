package com.v1.pension.model.pensioner;

import com.v1.pension.model.BaseModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Entity
@Table(name = "pensioner_virtual_presence")
public class PensionerVirtualPresence extends BaseModel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String pensionSubPNo; // foreign key for PensionRegistration

  private String registrationDate;

  @Column(name = "face_image_data", columnDefinition = "bytea")
  private byte[] faceImageData;

  @Column(name = "biometric_data1", columnDefinition = "bytea")
  private byte[] biometricData1;

  @Column(name = "biometric_data2", columnDefinition = "bytea")
  private byte[] biometricData2;

  private int status = 1;

  private int stage = 1;

  @Transient
  private MultipartFile faceImage;
}
