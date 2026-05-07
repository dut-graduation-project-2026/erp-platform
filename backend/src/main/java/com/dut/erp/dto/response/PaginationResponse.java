package com.dut.erp.dto.response;

public record PaginationResponse(
    int page, int limit, long totalItems, int totalPages, boolean hasNext, boolean hasPrev) {}
