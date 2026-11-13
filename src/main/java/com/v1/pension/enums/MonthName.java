package com.v1.pension.enums;

import lombok.Getter;

@Getter
public enum MonthName {
  BAISHAKH("Baishakh", 1, 10),
  JETH("Jeth", 2, 11),
  ASHAR("Ashar", 3, 12),
  SHRAWAN("Shrawan", 4, 1),
  BHADRA("Bhadra", 5, 2),
  ASHOJ("Ashoj", 6, 3),
  KARTIK("Kartik", 7, 4),
  MANGSIR("Mangsir", 8, 5),
  POUSH("Poush", 9, 6),
  MAGH("Magh", 10, 7),
  FALGUN("Falgun", 11, 8),
  CHAITRA("Chaitra", 12, 9);

  private final String name;
  private final int calendarMonth;
  private final int fyMonth;

  MonthName(String name, int calendarMonth, int fyMonth) {
    this.name = name;
    this.calendarMonth = calendarMonth;
    this.fyMonth = fyMonth;
  }

  public static MonthName getByCalendarMonth(int calendarMonth) {
    for (var mn : MonthName.values())
      if (mn.getCalendarMonth() == calendarMonth) return mn;

    throw new IllegalArgumentException("Calendar Month Not Found");
  }
}
