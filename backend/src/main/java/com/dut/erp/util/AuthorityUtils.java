package com.dut.erp.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AuthorityUtils {
  public static final String AUTHORITY_TEMPLATE = "%s:%s";

  public String formatAuthority(String resource, String action) {
    return String.format(AUTHORITY_TEMPLATE, resource, action);
  }

  public String extractPermissionResource(String authority) {
    if (authority == null || !authority.contains(":")) {
      return null;
    }
    return authority.split(":")[0];
  }

  public String extractPermissionAction(String authority) {
    if (authority == null || !authority.contains(":")) {
      return null;
    }
    return authority.split(":")[1];
  }
}
