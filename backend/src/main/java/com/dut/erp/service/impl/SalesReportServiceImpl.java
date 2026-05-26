package com.dut.erp.service.impl;

import com.dut.erp.dto.response.SalesDashboardResponse;
import com.dut.erp.dto.response.SalesDashboardResponse.PipelineStageCount;
import com.dut.erp.enums.LeadType;
import com.dut.erp.enums.SaleOrderStatus;
import com.dut.erp.repository.CrmLeadRepository;
import com.dut.erp.repository.SaleInvoiceRepository;
import com.dut.erp.repository.SaleOrderRepository;
import com.dut.erp.service.SalesReportService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalesReportServiceImpl implements SalesReportService {

  private final CrmLeadRepository crmLeadRepository;
  private final SaleOrderRepository saleOrderRepository;
  private final SaleInvoiceRepository saleInvoiceRepository;

  @Override
  public SalesDashboardResponse getDashboard(UUID organizationId) {
    log.info("Generating sales dashboard for organization {}", organizationId);

    // Leads & Opportunities
    Long totalLeads = countLeadsByType(organizationId, LeadType.LEAD);
    Long totalOpportunities = countLeadsByType(organizationId, LeadType.OPPORTUNITY);

    // Sale Orders
    Long totalConfirmedOrders = countOrdersByStatus(organizationId, SaleOrderStatus.CONFIRMED);
    Long totalDraftOrders = countOrdersByStatus(organizationId, SaleOrderStatus.DRAFT);
    BigDecimal totalRevenue = sumOrderTotalByStatus(organizationId, SaleOrderStatus.CONFIRMED);

    // Invoices
    Long totalInvoices = countInvoicesByOrganization(organizationId);
    Long unpaidInvoices = countUnpaidInvoices(organizationId);
    BigDecimal totalPaidAmount = sumPaidAmountByOrganization(organizationId);
    BigDecimal totalOutstandingAmount = totalRevenue.subtract(totalPaidAmount);

    // Pipeline by stage
    List<PipelineStageCount> pipeline = getPipelineByStage(organizationId);

    return new SalesDashboardResponse(
        totalLeads,
        totalOpportunities,
        totalRevenue,
        totalPaidAmount,
        totalOutstandingAmount,
        totalConfirmedOrders,
        totalDraftOrders,
        totalInvoices,
        unpaidInvoices,
        pipeline);
  }

  // ---- Private JPQL queries ----

  private Long countLeadsByType(UUID organizationId, LeadType type) {
    return crmLeadRepository.countByOrganizationIdAndType(organizationId, type.name());
  }

  private Long countOrdersByStatus(UUID organizationId, SaleOrderStatus status) {
    return saleOrderRepository.countByOrganizationIdAndStatus(organizationId, status.name());
  }

  private BigDecimal sumOrderTotalByStatus(UUID organizationId, SaleOrderStatus status) {
    return saleOrderRepository.sumTotalAmountByOrganizationIdAndStatus(
        organizationId, status.name());
  }

  private Long countInvoicesByOrganization(UUID organizationId) {
    return saleInvoiceRepository.countByOrganizationId(organizationId);
  }

  private Long countUnpaidInvoices(UUID organizationId) {
    return saleInvoiceRepository.countUnpaidByOrganizationId(organizationId);
  }

  private BigDecimal sumPaidAmountByOrganization(UUID organizationId) {
    return saleInvoiceRepository.sumPaidAmountByOrganizationId(organizationId);
  }

  private List<PipelineStageCount> getPipelineByStage(UUID organizationId) {
    return crmLeadRepository.countAndSumRevenueByStage(organizationId).stream()
        .map(
            row ->
                new PipelineStageCount(
                    (String) row[0],
                    ((Number) row[1]).longValue(),
                    row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO))
        .toList();
  }
}
