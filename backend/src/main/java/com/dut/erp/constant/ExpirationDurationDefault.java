package com.dut.erp.constant;

public class ExpirationDurationDefault {

  private ExpirationDurationDefault() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static final long INVITATION_EXPIRATION_DURATION_MS = 7 * 24 * 60 * 60 * 1000L; // 7 days

  public static final long RESEND_INVITATION_INTERVAL_MS = 2 * 60 * 1000L; // 2 minutes
}
