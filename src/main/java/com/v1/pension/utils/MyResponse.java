package com.v1.pension.utils;

import jakarta.persistence.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyResponse {
  public static Map<String, String> message(String msg) {
    return Map.of("message", msg);
  }

  public static Map<String, Object> message(String msg, long id) {
    return Map.of("message", msg, "id", id);
  }

  public static List<Map<String, Object>> data(List<Tuple> tuples) {
    var result = new ArrayList<Map<String, Object>>();
    tuples.forEach(tuple -> result.add(data(tuple)));
    return result;
  }

  public static Map<String, Object> data(Tuple tuple) {
    Map<String, Object> values = new HashMap<>();
    tuple.getElements().forEach(element -> {
      values.put(element.getAlias(), tuple.get(element));
    });
    return values;
  }
}
