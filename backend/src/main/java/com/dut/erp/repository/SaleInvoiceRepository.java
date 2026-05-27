package com.dut.erp.repository;

import com.dut.erp.entity.SaleInvoice;
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
public interface SaleInvoiceRepository extends JpaRepository<SaleInvoice, UUID> {

  Optional<SaleInvoice> findByInvoiceNumberAndOrganizationId(
      String invoiceNumber, UUID organizationId);

  @Query("""
      SELECT si.id
      FROM SaleInvoice si
      WHERE si.organization.id = :organizationId
      """)
  Page<UUID> findIdsByOrganizationId(
      @Param("organizationId") UUID organizationId, Pageable pageable);

  @Query("""
      SELECT DISTINCT si FROM SaleInvoice si
      LEFT JOIN FETCH si.partner
      LEFT JOIN FETCH si.order
      WHERE si.id IN :ids
      """)
  List<SaleInvoice> findAllByIdIn(@Param("ids") List<UUID> ids);

  @Query("""
      SELECT si FROM SaleInvoice si
      LEFT JOIN FETCH si.organization
      LEFT JOIN FETCH si.partner
      LEFT JOIN FETCH si.order
      WHERE si.id = :id
      """)
  Optional<SaleInvoice> findByIdWithDetails(@Param("id") UUID id);

  @Query("""
      SELECT COUNT(si)
      FROM SaleInvoice si
      WHERE si.organization.id = :organizationId
      """)
  Long countByOrganizationId(@Param("organizationId") UUID organizationId);

  @Query("""
      SELECT COUNT(si)
      FROM SaleInvoice si
      WHERE si.organization.id = :organizationId
      AND si.status NOT IN ('PAID', 'CANCELLED')
      """)
  Long countUnpaidByOrganizationId(@Param("organizationId") UUID organizationId);

  @Query("""
      SELECT COALESCE(SUM(si.paidAmount), 0)
      FROM SaleInvoice si
      WHERE si.organization.id = :organizationId
      """)
  java.math.BigDecimal sumPaidAmountByOrganizationId(@Param("organizationId") UUID organizationId);
}
