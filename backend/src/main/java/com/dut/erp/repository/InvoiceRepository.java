package com.dut.erp.repository;

import com.dut.erp.entity.Invoice;
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
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

  @Query(
      """
      SELECT i FROM Invoice i
      LEFT JOIN FETCH i.organization
      LEFT JOIN FETCH i.order
      LEFT JOIN FETCH i.partner
      LEFT JOIN FETCH i.createdBy
      LEFT JOIN FETCH i.updatedBy
      WHERE i.id = :id AND i.organization.id = :organizationId
      """)
  Optional<Invoice> findByIdAndOrganizationId(
      @Param("id") UUID id, @Param("organizationId") UUID organizationId);

  @Query(
      """
      SELECT i FROM Invoice i
      LEFT JOIN FETCH i.organization
      LEFT JOIN FETCH i.order
      LEFT JOIN FETCH i.partner
      LEFT JOIN FETCH i.createdBy
      LEFT JOIN FETCH i.updatedBy
      WHERE i.order.id = :orderId AND i.organization.id = :organizationId
      """)
  Optional<Invoice> findByOrderIdAndOrganizationId(
      @Param("orderId") UUID orderId, @Param("organizationId") UUID organizationId);

  @Query(
      """
      SELECT i.id
      FROM Invoice i
      WHERE i.organization.id = :organizationId
        AND (:status IS NULL OR i.status = :status)
      ORDER BY 
        CASE WHEN i.status = com.dut.erp.enums.InvoiceStatus.PAID THEN 1 ELSE 0 END ASC,
        CASE WHEN i.dueDate IS NULL THEN 1 ELSE 0 END ASC,
        i.dueDate ASC,
        i.createdAt ASC
      """)
  Page<UUID> findInvoiceIdsByOrganizationId(
      @Param("organizationId") UUID organizationId,
      @Param("status") com.dut.erp.enums.InvoiceStatus status,
      Pageable pageable);

  @Query(
      """
      SELECT i.id
      FROM Invoice i
      LEFT JOIN i.order o
      LEFT JOIN i.partner p
      WHERE i.organization.id = :organizationId
        AND (:status IS NULL OR i.status = :status)
        AND (LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :search, '%'))
             OR LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :search, '%'))
             OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
      ORDER BY 
        CASE WHEN i.status = com.dut.erp.enums.InvoiceStatus.PAID THEN 1 ELSE 0 END ASC,
        CASE WHEN i.dueDate IS NULL THEN 1 ELSE 0 END ASC,
        i.dueDate ASC,
        i.createdAt ASC
      """)
  Page<UUID> findInvoiceIdsByOrganizationIdAndSearch(
      @Param("organizationId") UUID organizationId,
      @Param("search") String search,
      @Param("status") com.dut.erp.enums.InvoiceStatus status,
      Pageable pageable);

  @Query(
      """
      SELECT DISTINCT i FROM Invoice i
      LEFT JOIN FETCH i.organization
      LEFT JOIN FETCH i.order
      LEFT JOIN FETCH i.partner
      LEFT JOIN FETCH i.createdBy
      LEFT JOIN FETCH i.updatedBy
      WHERE i.id IN :ids
      """)
  List<Invoice> findAllByIdIn(@Param("ids") List<UUID> ids);

  boolean existsByOrganizationIdAndInvoiceNumber(UUID organizationId, String invoiceNumber);

  boolean existsByOrderIdAndStatus(UUID orderId, com.dut.erp.enums.InvoiceStatus status);

  boolean existsByOrderIdAndStatusIn(UUID orderId, java.util.Collection<com.dut.erp.enums.InvoiceStatus> statuses);

  boolean existsByOrderIdAndStatusInAndIdNot(UUID orderId, java.util.Collection<com.dut.erp.enums.InvoiceStatus> statuses, UUID id);
}
