package com.v1.pension.repository.system;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.v1.pension.model.system.NepaliCalendar;

public interface NepaliCalendarRepository extends JpaRepository<NepaliCalendar, String> {
  @Query(nativeQuery = true, value = """
      select * from nepali_calendar
      where nepali_year = :year and nepali_month = :month and nepali_day = :day
      limit 1
      """)
  Optional<NepaliCalendar> findByYearAndMonthAndDay(int year, int month, int day);

  Optional<NepaliCalendar> findByRomanDate(LocalDate romanDate);

  @Query(nativeQuery = true, value = """
      select * from nepali_calendar
      where nepali_date > :nepaliDate
      order by nepali_date
      limit 1
      """)
  Optional<NepaliCalendar> getNextDay(String nepaliDate);
}
