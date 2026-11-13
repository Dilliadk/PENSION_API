package com.v1.pension.repository.system;

import org.springframework.data.jpa.repository.JpaRepository;

import com.v1.pension.model.system.NepaliDate;

public interface NepaliDateRepository extends JpaRepository<NepaliDate, Long> {
  // @Query(nativeQuery = true, value = """
  //     select * from nepali_dates
  //     where nepali_year = :year and nepali_month = :month and nepali_day = :day
  //     limit 1
  //     """)
  // Optional<NepaliDate> findByYearAndMonthAndDay(int year, int month, int day);

  // @Query(nativeQuery = true, value = """
  //     select * from nepali_dates
  //     where nepali_date > :nepaliDate
  //     order by nepali_date
  //     limit 1
  //     """)
  // Optional<NepaliDate> getNextDay(long nepaliDate);

  // Optional<NepaliDate> findByNepaliDate(long nepaliDate);
}
