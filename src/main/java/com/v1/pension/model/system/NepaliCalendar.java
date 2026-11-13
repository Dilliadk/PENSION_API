package com.v1.pension.model.system;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nepali_calendar")
public class NepaliCalendar {
  @Id
  private String nepaliDate;

  private int nepaliYear;

  private int nepaliMonth;

  private int nepaliDay;

  private int nepaliDaysInMonth;

  @Column(columnDefinition = "date")
  private LocalDate romanDate;

  private String day;
}
