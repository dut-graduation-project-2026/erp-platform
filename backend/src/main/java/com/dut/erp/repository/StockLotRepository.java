package com.dut.erp.repository;

import com.dut.erp.entity.StockLot;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockLotRepository extends JpaRepository<StockLot, UUID> {

  Optional<StockLot> findByIdAndOrganizationId(UUID id, UUID organizationId);

  Optional<StockLot> findByLotNumberAndProductIdAndOrganizationId(String lotNumber, UUID productId, UUID organizationId);

  List<StockLot> findAllByLotNumberAndOrganizationId(String lotNumber, UUID organizationId);

  List<StockLot> findAllByProductIdAndOrganizationId(UUID productId, UUID organizationId);

  List<StockLot> findAllByOrganizationIdAndExpirationDateBefore(UUID organizationId, java.time.Instant date);
}
