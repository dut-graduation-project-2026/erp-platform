package com.dut.erp.dto.response;

import java.util.List;
import org.springframework.data.domain.Page;

public record PagedEntityResponse<T>(List<T> data, PaginationResponse pagination) {
  public static <T> PagedEntityResponse<T> from(Page<T> page) {
    PaginationResponse pagination =
        new PaginationResponse(
            page.getNumber() + 1,
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.hasNext(),
            page.hasPrevious());

    return new PagedEntityResponse<T>(page.getContent(), pagination);
  }
}
