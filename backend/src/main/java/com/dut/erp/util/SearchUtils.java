package com.dut.erp.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SearchUtils {

  /**
   * Normalizes an optional search query filter by trimming it and collapsing
   * multiple consecutive spaces into a single space. Returns null if the query is empty.
   *
   * @param value the raw search query
   * @return the normalized search query, or null if the query is null or empty
   */
  public String normalizeOptionalFilter(String value) {
    if (value == null) {
      return null;
    }

    String trimmedValue = value.trim().replaceAll("\\s+", " ");
    return trimmedValue.isEmpty() ? null : trimmedValue;
  }

  /**
   * Escapes special characters used in SQL LIKE patterns ('\', '_', and '%')
   * to prevent wildcard injection.
   *
   * @param s the raw filter string
   * @return the escaped search pattern
   */
  public String escapeLikePattern(String s) {
    if (s == null) {
      return null;
    }
    return s.replace("\\", "\\\\").replace("_", "\\_").replace("%", "\\%");
  }
}
