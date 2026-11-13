package com.v1.pension.model.system;

import jakarta.persistence.*;
import lombok.Data;


    @Data
    @Entity
    @Table(name = "pensioner_other_types")
    public class PensionerOtherType {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(unique = true, nullable = false)
        private String otherTypeCode;

        private String otherTypeNameNep;
        private String otherTypeNameEng;
        private int status;
    }