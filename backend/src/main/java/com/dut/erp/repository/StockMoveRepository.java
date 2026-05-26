package com.dut.erp.repository;

import com.dut.erp.entity.StockMove;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockMoveRepository extends JpaRepository<StockMove, UUID> {

  List<StockMove> findAllByPickingId(UUID pickingId);
}
