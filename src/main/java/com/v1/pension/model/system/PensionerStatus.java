package com.v1.pension.model.system;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "pensioner_status")
public class PensionerStatus {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        @Column(unique = true, nullable = false)
        private String statusCode;
        private String statusNameNep;
        private String statusNameEng;
        private int status;
}