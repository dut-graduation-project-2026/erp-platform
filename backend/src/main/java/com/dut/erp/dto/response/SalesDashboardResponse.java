package com.dut.erp.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record SalesDashboardResponse(
    Long totalLeads,
    Long totalOpportunities,
    BigDecimal totalRevenue,
    BigDecimal totalPaidAmount,
    BigDecimal totalOutstandingAmount,
    Long totalConfirmedOrders,
    Long totalDraftOrders,
    Long totalInvoices,
    Long unpaidInvoices,
    List<PipelineStageCount> pipeline) {

  public record PipelineStageCount(String stageName, Long count, BigDecimal totalRevenue) {}
}
