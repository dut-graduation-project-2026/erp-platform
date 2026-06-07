package com.dut.erp.enums;

public enum TaxComputation {
  PERCENTAGE("Percentage"),
  FIXED_AMOUNT("Fixed Amount");

  private final String displayName;

  TaxComputation(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
