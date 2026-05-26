package com.dut.erp.repository;

import com.dut.erp.entity.SaleOrderLine;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleOrderLineRepository extends JpaRepository<SaleOrderLine, UUID> {}
