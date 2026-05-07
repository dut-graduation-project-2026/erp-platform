package com.dut.erp.dto.common;

import org.springframework.data.domain.Sort;

public record SortField(String field, Sort.Direction direction) {
  public static SortField of(String field, Sort.Direction direction) {
    return new SortField(field, direction);
  }

  public static SortField asc(String field) {
    return new SortField(field, Sort.Direction.ASC);
  }

  public static SortField desc(String field) {
    return new SortField(field, Sort.Direction.DESC);
  }
}
