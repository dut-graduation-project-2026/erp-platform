package com.dut.erp.enums;

public enum ActionType {
  CREATE("Tạo mới"),
  UPDATE("Cập nhật"),
  DELETE("Xóa"),
  VIEW("Xem"),
  EXPORT("Xuất"),
  IMPORT("Nhập");

  private final String description;

  ActionType(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
