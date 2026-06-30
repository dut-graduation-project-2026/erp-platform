package com.dut.erp.repository;

import com.dut.erp.entity.StockValuation;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StockValuationRepository extends JpaRepository<StockValuation, UUID> {
  
  @Query("""
      SELECT sv FROM StockValuation sv
      JOIN FETCH sv.inventoryDocumentLine line
      JOIN FETCH line.inventoryDocument doc
      WHERE doc.referenceId = :orderId
      """)
  List<StockValuation> findAllByOrderId(@Param("orderId") UUID orderId);

  @Query("""
      SELECT COALESCE(SUM(sv.totalValuation), 0)
      FROM StockValuation sv
      JOIN sv.inventoryDocumentLine line
      JOIN line.inventoryDocument doc
      JOIN Order o ON o.id = doc.referenceId
      WHERE o.organization.id = :organizationId
        AND o.status IN (com.dut.erp.enums.OrderStatus.CONFIRMED, com.dut.erp.enums.OrderStatus.COMPLETED)
        AND o.createdAt >= :startDate AND o.createdAt <= :endDate
      """)
  java.math.BigDecimal sumCogsByOrganizationIdAndOrderDateRange(
      @Param("organizationId") UUID organizationId,
      @Param("startDate") java.time.Instant startDate,
      @Param("endDate") java.time.Instant endDate);
}
