package com.dut.erp.util;

import com.dut.erp.security.CustomUserDetails;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@UtilityClass
public class SecurityUtils {
  public static CustomUserDetails getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
      return (CustomUserDetails) auth.getPrincipal();
    }
    throw new org.springframework.security.authentication.InsufficientAuthenticationException("User not authenticated");
  }
}
