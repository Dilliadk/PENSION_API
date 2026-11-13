package com.v1.pension.enums;

import lombok.Getter;

@Getter
public enum FormType {
  VOUCHER_TRANSACTION(1, "Voucher Transaction");

  private final int id;
  private final String name;

  FormType(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public static FormType fromId(Integer id) {
    if (id == null) return null;
    for (var ft : FormType.values()) {
      if (ft.getId() == id) return ft;
    }

    throw new IllegalArgumentException("No Form Type Found With Id: " + id);
  }
}
