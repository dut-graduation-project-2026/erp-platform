package com.dut.erp.repository;

import com.dut.erp.entity.Order;
import java.util.Optional;
import java.util.UUID;
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
      @Param("id") UUID id,
      @Param("organizationId") UUID organizationId);
}
