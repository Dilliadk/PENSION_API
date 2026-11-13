package com.v1.pension.model.system;

import com.v1.pension.model.BaseModel;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "pension_age_brackets")
@Data
@EqualsAndHashCode(callSuper = true)
public class PensionAgeBracket extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer ageFrom;

    @Column(nullable = false)
    private Integer ageTo; // for open-ended, set e.g. 999

    @Column(nullable = false)
    private String label; // e.g., "60+", "75+", "85+"

    @Column(nullable = false)
    private Double extraAmount; // extra monthly amount for this age
}