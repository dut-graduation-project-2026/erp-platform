package com.dut.erp.repository;

import com.dut.erp.entity.OrderItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

  List<OrderItem> findAllByOrderIdAndOrganizationId(UUID orderId, UUID organizationId);

  @Query(
      """
      SELECT oi FROM OrderItem oi
      LEFT JOIN FETCH oi.product
      LEFT JOIN FETCH oi.tax
      WHERE oi.id = :id AND oi.order.id = :orderId AND oi.organization.id = :organizationId
      """)
  Optional<OrderItem> findByIdAndOrderIdAndOrganizationId(
      @Param("id") UUID id,
      @Param("orderId") UUID orderId,
      @Param("organizationId") UUID organizationId);
}
