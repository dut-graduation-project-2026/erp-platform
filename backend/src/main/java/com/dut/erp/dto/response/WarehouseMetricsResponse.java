package com.dut.erp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WarehouseMetricsResponse(
    MetricDetail receipts,
    MetricDetail deliveries,
    MetricDetail internalTransfers,
    long pendingFulfillmentCount
) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MetricDetail(
        long toProcess,
        long backorders,
        long late
    ) {}
}
