package com.dut.erp.repository;

import com.dut.erp.entity.Order;
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
public interface OrderRepository extends JpaRepository<Order, UUID> {

  @Query(
      """
      SELECT o FROM Order o
      LEFT JOIN FETCH o.items
      WHERE o.id = :id AND o.organization.id = :organizationId
      """)
  Optional<Order> findByIdAndOrganizationId(
      @Param("id") UUID id, @Param("organizationId") UUID organizationId);

  /** Lightweight lookup — does NOT fetch the items collection. Use for writes that don't need items. */
  @Query(
      """
      SELECT o FROM Order o
      WHERE o.id = :id AND o.organization.id = :organizationId
      """)
  Optional<Order> findShallowByIdAndOrganizationId(
      @Param("id") UUID id, @Param("organizationId") UUID organizationId);

  /** Lookup that fetches the lead to avoid N+1 query when updating status. */
  @Query(
      """
      SELECT o FROM Order o
      LEFT JOIN FETCH o.lead
      WHERE o.id = :id AND o.organization.id = :organizationId
      """)
  Optional<Order> findWithLeadByIdAndOrganizationId(
      @Param("id") UUID id, @Param("organizationId") UUID organizationId);

  // --- Quotations (status = DRAFT) ---
  @Query(
      """
      SELECT o.id
      FROM Order o
      WHERE o.organization.id = :organizationId
      AND o.status = com.dut.erp.enums.OrderStatus.DRAFT
      """)
  Page<UUID> findQuotationIdsByOrganizationId(
      @Param("organizationId") UUID organizationId, Pageable pageable);

  @Query(
      """
      SELECT o.id
      FROM Order o
      WHERE o.organization.id = :organizationId
      AND o.status = com.dut.erp.enums.OrderStatus.DRAFT
      AND LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :search, '%'))
      """)
  Page<UUID> findQuotationIdsByOrganizationIdAndSearch(
      @Param("organizationId") UUID organizationId,
      @Param("search") String search,
      Pageable pageable);

  // --- Orders (status != DRAFT) ---
  @Query(
      """
      SELECT o.id
      FROM Order o
      WHERE o.organization.id = :organizationId
      AND o.status <> com.dut.erp.enums.OrderStatus.DRAFT
      """)
  Page<UUID> findOrderIdsByOrganizationId(
      @Param("organizationId") UUID organizationId, Pageable pageable);

  @Query(
      """
      SELECT o.id
      FROM Order o
      WHERE o.organization.id = :organizationId
      AND o.status <> com.dut.erp.enums.OrderStatus.DRAFT
      AND LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :search, '%'))
      """)
  Page<UUID> findOrderIdsByOrganizationIdAndSearch(
      @Param("organizationId") UUID organizationId,
      @Param("search") String search,
      Pageable pageable);

  @Query(
      """
      SELECT DISTINCT o FROM Order o
      LEFT JOIN FETCH o.organization
      LEFT JOIN FETCH o.partner
      LEFT JOIN FETCH o.lead
      LEFT JOIN FETCH o.createdBy
      LEFT JOIN FETCH o.updatedBy
      WHERE o.id IN :ids
      """)
  List<Order> findAllByIdIn(@Param("ids") List<UUID> ids);

  @Query(
      """
      SELECT o.id
      FROM Order o
      WHERE o.organization.id = :organizationId
      AND o.status = :status
      """)
  Page<UUID> findIdsByOrganizationIdAndStatus(
      @Param("organizationId") UUID organizationId,
      @Param("status") com.dut.erp.enums.OrderStatus status,
      Pageable pageable);

  boolean existsByOrganizationIdAndOrderNumber(UUID organizationId, String orderNumber);
}
