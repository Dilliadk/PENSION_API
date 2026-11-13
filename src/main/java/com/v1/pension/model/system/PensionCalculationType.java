package com.v1.pension.model.system;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "pension_calculation_type")
public class PensionCalculationType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String calculationCode;
    private String calculationNameNep;
    private String calculationNameEng;
    private int status;
}
