package com.v1.pension.model.system;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "nepali_dates")
public class NepaliDate {
  @Id
  private Long nepaliDate;

  @Column(columnDefinition = "date")
  private Date romanDate;

  private int nepaliDay;

  private int nepaliMonth;

  private int nepaliYear;

  private int nepaliDaysInMonth;
}
