package com.dut.erp.enums;

public enum NotificationType {
  INFO("INFO", "Thông tin chung"),
  WARNING("WARNING", "Cảnh báo"),
  ERROR("ERROR", "Lỗi hệ thống"),
  SUCCESS("SUCCESS", "Thành công"),
  ALERT("ALERT", "Cảnh báo khẩn cấp");

  private final String code;
  private final String description;

  NotificationType(String code, String description) {
    this.code = code;
    this.description = description;
  }

  public String getCode() {
    return code;
  }

  public String getDescription() {
    return description;
  }

  // Tìm enum theo code (nếu cần)
  public static NotificationType fromCode(String code) {
    for (NotificationType type : values()) {
      if (type.code.equalsIgnoreCase(code)) {
        return type;
      }
    }
    return INFO; // mặc định
  }
}
