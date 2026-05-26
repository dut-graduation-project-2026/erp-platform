package com.dut.erp.repository;

import com.dut.erp.entity.StockValuation;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StockValuationRepository extends JpaRepository<StockValuation, UUID> {

  List<StockValuation> findAllByProductIdAndRemainingQtyGreaterThanOrderByCreatedAtAsc(UUID productId, BigDecimal remainingQtyLimit);

  List<StockValuation> findAllByProductIdAndRemainingQtyGreaterThanOrderByCreatedAtDesc(UUID productId, BigDecimal remainingQtyLimit);

  List<StockValuation> findAllByProductIdOrderByCreatedAtDesc(UUID productId);

  List<StockValuation> findAllByProductOrganizationIdAndRemainingQtyGreaterThan(UUID organizationId, BigDecimal remainingQtyLimit);

  @Query("SELECT COALESCE(SUM(sv.remainingValue), 0) FROM StockValuation sv WHERE sv.product.organization.id = :organizationId AND sv.remainingQty > 0")
  BigDecimal sumRemainingValueByOrganizationId(@Param("organizationId") UUID organizationId);

  @Query("SELECT COALESCE(SUM(sv.totalValue), 0) FROM StockValuation sv WHERE sv.product.organization.id = :organizationId AND sv.quantity < 0 AND sv.createdAt >= :startDate")
  BigDecimal sumOutboundValueByOrganizationIdAndDateAfter(@Param("organizationId") UUID organizationId, @Param("startDate") Instant startDate);

  @Query("SELECT COALESCE(SUM(sv.totalValue), 0) FROM StockValuation sv WHERE sv.product.organization.id = :organizationId AND sv.createdAt <= :date")
  BigDecimal sumTotalValueByOrganizationIdAndDateBefore(@Param("organizationId") UUID organizationId, @Param("date") Instant date);
}

