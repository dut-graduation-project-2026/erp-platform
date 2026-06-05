package com.dut.erp.repository;

import com.dut.erp.entity.Product;
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
public interface ProductRepository extends JpaRepository<Product, UUID> {

  @Query(
      """
      SELECT p.id
      FROM Product p
      WHERE p.organization.id = :organizationId
      """)
  Page<UUID> findIdsByOrganizationId(
      @Param("organizationId") UUID organizationId, Pageable pageable);

  @Query(
      """
      SELECT p.id
      FROM Product p
      WHERE p.organization.id = :organizationId
      AND LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
      """)
  Page<UUID> findIdsByOrganizationIdAndSearch(
      @Param("organizationId") UUID organizationId,
      @Param("search") String search,
      Pageable pageable);

  @Query(
      """
      SELECT DISTINCT p FROM Product p
      LEFT JOIN FETCH p.organization
      WHERE p.id IN :ids
      """)
  List<Product> findAllByIdIn(@Param("ids") List<UUID> ids);
  Optional<Product> findByIdAndOrganizationId(UUID id, UUID organizationId);
}
