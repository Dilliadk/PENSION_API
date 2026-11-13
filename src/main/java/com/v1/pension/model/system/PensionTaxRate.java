package com.v1.pension.model.system;

import com.v1.pension.model.BaseModel;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "pension_tax_rates")
@Data
@EqualsAndHashCode(callSuper = true)
public class PensionTaxRate extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer year; // e.g., 2024

    @Column(nullable = false)
    private Double rate; // e.g., 0.15 for 15%
}