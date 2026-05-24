package com.dut.erp.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PermissionActionUtils {
  public static final String AUTHORITY_TEMPLATE = "%s:%s";

  public String formatAuthority(String resource, String action) {
    return String.format(AUTHORITY_TEMPLATE, resource, action);
  }

  public String extractModule(String authority) {
    if (authority == null || !authority.contains(":")) {
      return null;
    }
    String[] parts = authority.split(":", 2);
    if (parts[0].isEmpty()) {
      return null;
    }
    return parts[0];
  }

  public String extractModulePermission(String authority) {
    if (authority == null || !authority.contains(":")) {
      return null;
    }
    String[] parts = authority.split(":", 2);
    if (parts.length < 2 || parts[1].isEmpty()) {
      return null;
    }
    return parts[1];
  }
}
