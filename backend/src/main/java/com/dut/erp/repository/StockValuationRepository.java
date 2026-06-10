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
}
