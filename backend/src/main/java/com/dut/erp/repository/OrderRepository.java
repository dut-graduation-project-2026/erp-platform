package com.dut.erp.repository;

import com.dut.erp.entity.Order;
import com.dut.erp.repository.projection.SalesBySalespersonProjection;
import com.dut.erp.repository.projection.SalesSummaryProjection;
import com.dut.erp.repository.projection.RevenueTrendProjection;
import com.dut.erp.repository.projection.TopCustomerProjection;
import com.dut.erp.repository.projection.TopProductProjection;
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

  boolean existsByOrganizationIdAndOrderNumber(UUID organizationId, String orderNumber);

  // --- Sales Analytics Queries ---

  @Query(
      """
      SELECT
          COALESCE(SUM(CASE WHEN o.status <> com.dut.erp.enums.OrderStatus.DRAFT AND o.status <> com.dut.erp.enums.OrderStatus.CANCELLED THEN o.totalAmount ELSE 0 END), 0) AS totalRevenue,
          COUNT(CASE WHEN o.status <> com.dut.erp.enums.OrderStatus.DRAFT AND o.status <> com.dut.erp.enums.OrderStatus.CANCELLED THEN 1 END) AS totalOrders,
          COALESCE(AVG(CASE WHEN o.status <> com.dut.erp.enums.OrderStatus.DRAFT AND o.status <> com.dut.erp.enums.OrderStatus.CANCELLED THEN o.totalAmount END), 0) AS averageOrderValue,
          COUNT(CASE WHEN o.status = com.dut.erp.enums.OrderStatus.COMPLETED THEN 1 END) AS completedOrders,
          COUNT(CASE WHEN o.status = com.dut.erp.enums.OrderStatus.CANCELLED THEN 1 END) AS cancelledOrders
      FROM Order o
      WHERE o.organization.id = :organizationId
        AND o.createdAt >= :startDate
        AND o.createdAt <= :endDate
      """)
  SalesSummaryProjection getSalesSummary(
      @Param("organizationId") UUID organizationId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  @Query(
      value =
          """
          SELECT
              TO_CHAR(o.created_at, 'YYYY-MM-DD') AS dateVal,
              SUM(o.total_amount) AS revenue,
              COUNT(o.id) AS orderCount
          FROM orders o
          WHERE o.organization_id = :organizationId
            AND o.status <> 'DRAFT' AND o.status <> 'CANCELLED'
            AND o.created_at >= :startDate
            AND o.created_at <= :endDate
          GROUP BY TO_CHAR(o.created_at, 'YYYY-MM-DD')
          ORDER BY dateVal ASC
          """,
      nativeQuery = true)
  List<RevenueTrendProjection> getDailyRevenueTrend(
      @Param("organizationId") UUID organizationId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  @Query(
      value =
          """
          SELECT
              TO_CHAR(DATE_TRUNC('week', o.created_at), 'YYYY-MM-DD') AS dateVal,
              SUM(o.total_amount) AS revenue,
              COUNT(o.id) AS orderCount
          FROM orders o
          WHERE o.organization_id = :organizationId
            AND o.status <> 'DRAFT' AND o.status <> 'CANCELLED'
            AND o.created_at >= :startDate
            AND o.created_at <= :endDate
          GROUP BY DATE_TRUNC('week', o.created_at)
          ORDER BY DATE_TRUNC('week', o.created_at) ASC
          """,
      nativeQuery = true)
  List<RevenueTrendProjection> getWeeklyRevenueTrend(
      @Param("organizationId") UUID organizationId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  @Query(
      value =
          """
          SELECT
              TO_CHAR(DATE_TRUNC('month', o.created_at), 'YYYY-MM') AS dateVal,
              SUM(o.total_amount) AS revenue,
              COUNT(o.id) AS orderCount
          FROM orders o
          WHERE o.organization_id = :organizationId
            AND o.status <> 'DRAFT' AND o.status <> 'CANCELLED'
            AND o.created_at >= :startDate
            AND o.created_at <= :endDate
          GROUP BY DATE_TRUNC('month', o.created_at)
          ORDER BY DATE_TRUNC('month', o.created_at) ASC
          """,
      nativeQuery = true)
  List<RevenueTrendProjection> getMonthlyRevenueTrend(
      @Param("organizationId") UUID organizationId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  @Query(
      value =
          """
          SELECT
              p.id AS partnerId,
              p.name AS partnerName,
              SUM(o.total_amount) AS totalSpend,
              COUNT(o.id) AS orderCount
          FROM orders o
          JOIN partners p ON o.partner_id = p.id
          WHERE o.organization_id = :organizationId
            AND o.status <> 'DRAFT' AND o.status <> 'CANCELLED'
            AND o.created_at >= :startDate
            AND o.created_at <= :endDate
          GROUP BY p.id, p.name
          ORDER BY totalSpend DESC
          LIMIT :limitVal
          """,
      nativeQuery = true)
  List<TopCustomerProjection> getTopCustomers(
      @Param("organizationId") UUID organizationId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate,
      @Param("limitVal") int limitVal);

  @Query(
      value =
          """
          SELECT
              pr.id AS productId,
              pr.name AS productName,
              SUM(oi.subtotal) AS totalRevenue,
              SUM(oi.quantity) AS quantitySold
          FROM order_items oi
          JOIN orders o ON oi.order_id = o.id
          JOIN products pr ON oi.product_id = pr.id
          WHERE o.organization_id = :organizationId
            AND o.status <> 'DRAFT' AND o.status <> 'CANCELLED'
            AND o.created_at >= :startDate
            AND o.created_at <= :endDate
          GROUP BY pr.id, pr.name
          ORDER BY totalRevenue DESC
          LIMIT :limitVal
          """,
      nativeQuery = true)
  List<TopProductProjection> getTopProducts(
      @Param("organizationId") UUID organizationId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate,
      @Param("limitVal") int limitVal);

  @Query(
      value =
          """
          SELECT
              u.id AS salespersonId,
              CONCAT(u.first_name, ' ', u.last_name) AS salespersonName,
              SUM(o.total_amount) AS totalRevenue,
              COUNT(o.id) AS orderCount
          FROM orders o
          JOIN users u ON o.created_by = u.id
          WHERE o.organization_id = :organizationId
            AND o.status <> 'DRAFT' AND o.status <> 'CANCELLED'
            AND o.created_at >= :startDate
            AND o.created_at <= :endDate
          GROUP BY u.id, u.first_name, u.last_name
          ORDER BY totalRevenue DESC
          """,
      nativeQuery = true)
  List<SalesBySalespersonProjection> getSalesBySalesperson(
      @Param("organizationId") UUID organizationId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);
}
