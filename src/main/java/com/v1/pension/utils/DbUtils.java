package com.v1.pension.utils;

import java.util.ArrayList;
import java.util.Arrays;

public class DbUtils {
  public static Integer getReceiveStageCodeForRole(String role) {
    return switch (role) {
      case "PMO_CREATOR" -> 1;
      case "PMO_REVIEWER" -> 2;
      case "PMO_APPROVER" -> 3;
      case "BANK_CREATOR" -> 5;
      case "BANK_REVIEWER" -> 6;
      case "BANK_APPROVER" -> 7;
      case "DTCO_CREATOR" -> 9;
      case "DTCO_REVIEWER" -> 10;
      case "DTCO_APPROVER" -> 11;
      default -> 0;
    };
  }

  public static String getUserIds(String initialValue, Long add1, Long add2, Long remove1, Long remove2) {
    if (initialValue.isEmpty()) {
      var userIds = new ArrayList<String>();
      if (add1 != null) userIds.add(Long.toString(add1));
      if (add2 != null) userIds.add(Long.toString(add2));

      return String.join(",", userIds);
    } else {
      // 1, 2
      var userIds = new java.util.ArrayList<>(Arrays.stream(initialValue.split(",")).toList());
      if (remove1 != null) userIds.remove(Long.toString(remove1));
      if (remove2 != null) userIds.remove(Long.toString(remove2));

      // only add if it doesn't exist yet
      if (add1 != null) {
        var id = Long.toString(add1);
        if (!userIds.contains(id)) userIds.add(id);
      }
      if (add2 != null) {
        var id = Long.toString(add2);
        if (!userIds.contains(id)) userIds.add(id);
      }

      return String.join(",", userIds);
    }
  }
}
