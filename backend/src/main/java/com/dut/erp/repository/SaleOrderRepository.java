package com.dut.erp.repository;

import com.dut.erp.entity.SaleOrder;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleOrderRepository extends JpaRepository<SaleOrder, UUID> {

  Optional<SaleOrder> findByOrderNumberAndOrganizationId(String orderNumber, UUID organizationId);

  @Query("""
      SELECT so.id
      FROM SaleOrder so
      WHERE so.organization.id = :organizationId
      """)
  Page<UUID> findIdsByOrganizationId(
      @Param("organizationId") UUID organizationId, Pageable pageable);

  @Query("""
      SELECT DISTINCT so FROM SaleOrder so
      LEFT JOIN FETCH so.partner
      LEFT JOIN FETCH so.organization
      WHERE so.id IN :ids
      """)
  List<SaleOrder> findAllByIdIn(@Param("ids") List<UUID> ids);

  @Query("""
      SELECT so FROM SaleOrder so
      LEFT JOIN FETCH so.organization
      LEFT JOIN FETCH so.partner
      LEFT JOIN FETCH so.opportunity
      LEFT JOIN FETCH so.salesperson
      LEFT JOIN FETCH so.lines l
      LEFT JOIN FETCH l.product
      WHERE so.id = :id
      """)
  Optional<SaleOrder> findByIdWithDetails(@Param("id") UUID id);

  @Query("""
      SELECT COUNT(so)
      FROM SaleOrder so
      WHERE so.organization.id = :organizationId
      AND so.status = :status
      """)
  Long countByOrganizationIdAndStatus(
      @Param("organizationId") UUID organizationId, @Param("status") com.dut.erp.enums.SaleOrderStatus status);

  @Query("""
      SELECT COALESCE(SUM(so.totalAmount), 0)
      FROM SaleOrder so
      WHERE so.organization.id = :organizationId
      AND so.status = :status
      """)
  java.math.BigDecimal sumTotalAmountByOrganizationIdAndStatus(
      @Param("organizationId") UUID organizationId, @Param("status") com.dut.erp.enums.SaleOrderStatus status);
}
