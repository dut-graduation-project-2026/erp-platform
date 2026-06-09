package com.dut.erp.repository;

import com.dut.erp.entity.InventoryTransaction;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, UUID> {
  List<InventoryTransaction> findAllByInventoryDocumentId(UUID inventoryDocumentId);
}
