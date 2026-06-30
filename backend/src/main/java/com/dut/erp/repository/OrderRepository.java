package com.dut.erp.repository;

import com.dut.erp.entity.Order;
import com.dut.erp.enums.OrderStatus;
import java.time.Instant;
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
      LEFT JOIN o.lead l
      LEFT JOIN o.partner p
      WHERE o.organization.id = :organizationId
      AND o.status = com.dut.erp.enums.OrderStatus.DRAFT
      AND (:search IS NULL OR :search = '' 
           OR LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
      AND (:saleTeamId IS NULL OR l.saleTeam.id = :saleTeamId)
      """)
  Page<UUID> findQuotationIdsWithFilters(
      @Param("organizationId") UUID organizationId,
      @Param("search") String search,
      @Param("saleTeamId") UUID saleTeamId,
      Pageable pageable);

  @Query(
      """
      SELECT o.id
      FROM Order o
      LEFT JOIN o.lead l
      LEFT JOIN o.partner p
      WHERE o.organization.id = :organizationId
      AND o.status = com.dut.erp.enums.OrderStatus.DRAFT
      AND (
        o.createdBy.id = :userId 
        OR l.salePerson.id = :userId 
        OR l.saleTeam.id IN :teamIds
      )
      AND (:search IS NULL OR :search = '' 
           OR LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
      AND (:saleTeamId IS NULL OR l.saleTeam.id = :saleTeamId)
      """)
  Page<UUID> findQuotationIdsWithFiltersAndUser(
      @Param("organizationId") UUID organizationId,
      @Param("search") String search,
      @Param("saleTeamId") UUID saleTeamId,
      @Param("userId") UUID userId,
      @Param("teamIds") List<UUID> teamIds,
      Pageable pageable);

  // --- Orders (status != DRAFT) ---
  @Query(
      """
      SELECT o.id
      FROM Order o
      WHERE o.organization.id = :organizationId
      AND o.status <> com.dut.erp.enums.OrderStatus.DRAFT
      AND (cast(:startDate as timestamp) IS NULL OR o.createdAt >= :startDate)
      AND (cast(:endDate as timestamp) IS NULL OR o.createdAt <= :endDate)
      """)
  Page<UUID> findOrderIdsByOrganizationId(
      @Param("organizationId") UUID organizationId,
      @Param("startDate") java.time.Instant startDate,
      @Param("endDate") java.time.Instant endDate,
      Pageable pageable);

  @Query(
      """
      SELECT o.id
      FROM Order o
      LEFT JOIN o.partner p
      WHERE o.organization.id = :organizationId
      AND o.status <> com.dut.erp.enums.OrderStatus.DRAFT
      AND (LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
      AND (cast(:startDate as timestamp) IS NULL OR o.createdAt >= :startDate)
      AND (cast(:endDate as timestamp) IS NULL OR o.createdAt <= :endDate)
      """)
  Page<UUID> findOrderIdsByOrganizationIdAndSearch(
      @Param("organizationId") UUID organizationId,
      @Param("search") String search,
      @Param("startDate") java.time.Instant startDate,
      @Param("endDate") java.time.Instant endDate,
      Pageable pageable);


  @Query(
      """
      SELECT o.id
      FROM Order o
      LEFT JOIN o.lead l
      LEFT JOIN o.partner p
      WHERE o.organization.id = :organizationId
      AND o.status <> com.dut.erp.enums.OrderStatus.DRAFT
      AND (:search IS NULL OR :search = '' 
           OR LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
      AND (:status IS NULL OR o.status = :status)
      AND (:partnerId IS NULL OR o.partner.id = :partnerId)
      AND (:salePersonId IS NULL OR l.salePerson.id = :salePersonId)
      AND (:saleTeamId IS NULL OR l.saleTeam.id = :saleTeamId)
      AND (cast(:startDate as timestamp) IS NULL OR o.createdAt >= :startDate)
      AND (cast(:endDate as timestamp) IS NULL OR o.createdAt <= :endDate)
      """)
  Page<UUID> findOrderIdsWithFilters(
      @Param("organizationId") UUID organizationId,
      @Param("search") String search,
      @Param("status") OrderStatus status,
      @Param("partnerId") UUID partnerId,
      @Param("salePersonId") UUID salePersonId,
      @Param("saleTeamId") UUID saleTeamId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate,
      Pageable pageable);

  @Query(
      """
      SELECT o.id
      FROM Order o
      LEFT JOIN o.lead l
      LEFT JOIN o.partner p
      WHERE o.organization.id = :organizationId
      AND o.status <> com.dut.erp.enums.OrderStatus.DRAFT
      AND (
        o.createdBy.id = :userId 
        OR l.salePerson.id = :userId 
        OR l.saleTeam.id IN :teamIds
      )
      AND (:search IS NULL OR :search = '' 
           OR LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
      AND (:status IS NULL OR o.status = :status)
      AND (:partnerId IS NULL OR o.partner.id = :partnerId)
      AND (:salePersonId IS NULL OR l.salePerson.id = :salePersonId)
      AND (:saleTeamId IS NULL OR l.saleTeam.id = :saleTeamId)
      AND (cast(:startDate as timestamp) IS NULL OR o.createdAt >= :startDate)
      AND (cast(:endDate as timestamp) IS NULL OR o.createdAt <= :endDate)
      """)
  Page<UUID> findOrderIdsWithFiltersAndUser(
      @Param("organizationId") UUID organizationId,
      @Param("search") String search,
      @Param("status") OrderStatus status,
      @Param("partnerId") UUID partnerId,
      @Param("salePersonId") UUID salePersonId,
      @Param("saleTeamId") UUID saleTeamId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate,
      @Param("userId") UUID userId,
      @Param("teamIds") List<UUID> teamIds,
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

  @Query(
      """
      SELECT o.id
      FROM Order o
      LEFT JOIN o.lead l
      WHERE o.organization.id = :organizationId
      AND o.status = :status
      AND (
        o.createdBy.id = :userId 
        OR l.salePerson.id = :userId 
        OR l.saleTeam.id IN :teamIds
      )
      """)
  Page<UUID> findIdsByOrganizationIdAndStatusAndUser(
      @Param("organizationId") UUID organizationId,
      @Param("status") com.dut.erp.enums.OrderStatus status,
      @Param("userId") UUID userId,
      @Param("teamIds") List<UUID> teamIds,
      Pageable pageable);

  @Query(
      """
      SELECT COALESCE(SUM(o.totalAmount), 0)
      FROM Order o
      WHERE o.organization.id = :organizationId
        AND o.status IN (com.dut.erp.enums.OrderStatus.CONFIRMED, com.dut.erp.enums.OrderStatus.COMPLETED)
        AND o.createdAt >= :startDate AND o.createdAt <= :endDate
      """)
  java.math.BigDecimal sumRevenueByOrganizationIdAndDateRange(
      @Param("organizationId") UUID organizationId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  @Query(
      """
      SELECT COALESCE(AVG(o.totalAmount), 0)
      FROM Order o
      WHERE o.organization.id = :organizationId
        AND o.status IN (com.dut.erp.enums.OrderStatus.CONFIRMED, com.dut.erp.enums.OrderStatus.COMPLETED)
        AND o.createdAt >= :startDate AND o.createdAt <= :endDate
      """)
  java.math.BigDecimal avgDealSizeByOrganizationIdAndDateRange(
      @Param("organizationId") UUID organizationId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  @Query(
      """
      SELECT COUNT(DISTINCT o.createdBy.id)
      FROM Order o
      WHERE o.organization.id = :organizationId
        AND o.createdAt >= :startDate AND o.createdAt <= :endDate
      """)
  long countActiveSalesRepsByOrganizationIdAndDateRange(
      @Param("organizationId") UUID organizationId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  @Query("""
      SELECT new com.dut.erp.dto.response.analytics.OrderStatusCount(o.status, COUNT(o.id))
      FROM Order o
      WHERE o.organization.id = :organizationId
        AND o.createdAt >= :startDate AND o.createdAt <= :endDate
      GROUP BY o.status
      """)
  List<com.dut.erp.dto.response.analytics.OrderStatusCount> countOrdersByStatusAndDateRange(
      @Param("organizationId") UUID organizationId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  boolean existsByOrganizationIdAndOrderNumber(UUID organizationId, String orderNumber);

  @Query("""
      SELECT pc.id, pc.name,
        COALESCE(SUM(oi.subtotal), 0),
        COALESCE(SUM(oi.quantity), 0),
        COUNT(DISTINCT o.id)
      FROM OrderItem oi
      JOIN oi.order o
      JOIN oi.product p
      JOIN p.category pc
      WHERE o.organization.id = :organizationId
        AND o.status IN (com.dut.erp.enums.OrderStatus.CONFIRMED, com.dut.erp.enums.OrderStatus.COMPLETED)
        AND (cast(:startDate as timestamp) IS NULL OR o.createdAt >= :startDate)
        AND (cast(:endDate as timestamp) IS NULL OR o.createdAt <= :endDate)
      GROUP BY pc.id, pc.name
      ORDER BY COALESCE(SUM(oi.subtotal), 0) DESC
      """)
  List<Object[]> findCategorySalesDistribution(
      @Param("organizationId") UUID organizationId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  @Query("""
      SELECT new com.dut.erp.dto.response.analytics.TopProductResponse(
        oi.product.id,
        oi.product.name,
        oi.product.category.name,
        COALESCE(SUM(oi.subtotal), 0),
        COALESCE(SUM(oi.quantity), 0),
        COUNT(DISTINCT o.id)
      )
      FROM OrderItem oi
      JOIN oi.order o
      WHERE o.organization.id = :organizationId
        AND o.status IN (com.dut.erp.enums.OrderStatus.CONFIRMED, com.dut.erp.enums.OrderStatus.COMPLETED)
        AND (cast(:startDate as timestamp) IS NULL OR o.createdAt >= :startDate)
        AND (cast(:endDate as timestamp) IS NULL OR o.createdAt <= :endDate)
      GROUP BY oi.product.id, oi.product.name, oi.product.category.name
      ORDER BY COALESCE(SUM(oi.subtotal), 0) DESC
      """)
  List<com.dut.erp.dto.response.analytics.TopProductResponse> findTopPerformingProducts(
      @Param("organizationId") UUID organizationId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate,
      Pageable pageable);

  @Query(
      """
      SELECT COUNT(o.id)
      FROM Order o
      WHERE o.organization.id = :organizationId
      AND o.status = com.dut.erp.enums.OrderStatus.CONFIRMED
      """)
  long countPendingFulfillmentOrders(@Param("organizationId") UUID organizationId);

  List<Order> findByOrganizationIdAndStatus(UUID organizationId, OrderStatus status);

  @Query(
      """
      SELECT DATE(o.createdAt), COALESCE(SUM(o.totalAmount), 0)
      FROM Order o
      WHERE o.organization.id = :organizationId
        AND o.status IN (com.dut.erp.enums.OrderStatus.CONFIRMED, com.dut.erp.enums.OrderStatus.COMPLETED)
        AND o.createdAt >= :startDate
      GROUP BY DATE(o.createdAt)
      ORDER BY DATE(o.createdAt) ASC
      """)
  List<Object[]> getDailyRevenue(
      @Param("organizationId") UUID organizationId, @Param("startDate") Instant startDate);

  @Query(
      """
      SELECT oi.product.id, oi.product.name, DATE(o.createdAt), SUM(oi.quantity), AVG(oi.unitPrice)
      FROM OrderItem oi
      JOIN oi.order o
      WHERE o.organization.id = :organizationId
        AND o.status IN (com.dut.erp.enums.OrderStatus.CONFIRMED, com.dut.erp.enums.OrderStatus.COMPLETED)
        AND o.createdAt >= :startDate
      GROUP BY oi.product.id, oi.product.name, DATE(o.createdAt)
      """)
  List<Object[]> getProductSalesHistory(
      @Param("organizationId") UUID organizationId, @Param("startDate") Instant startDate);
}

