package com.dut.erp.repository;

import com.dut.erp.entity.InventoryDocumentLine;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryDocumentLineRepository
    extends JpaRepository<InventoryDocumentLine, UUID> {
  List<InventoryDocumentLine> findAllByInventoryDocumentId(UUID inventoryDocumentId);
}
