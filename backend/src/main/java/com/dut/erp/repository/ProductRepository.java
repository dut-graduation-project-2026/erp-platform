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
      WHERE p.organization.id = :organizationId AND p.isArchived = :isArchived
      """)
  Page<UUID> findIdsByOrganizationIdAndIsArchived(
      @Param("organizationId") UUID organizationId,
      @Param("isArchived") boolean isArchived,
      Pageable pageable);

  @Query(
      """
      SELECT p.id
      FROM Product p
      WHERE p.organization.id = :organizationId
      AND p.isArchived = :isArchived
      AND LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
      """)
  Page<UUID> findIdsByOrganizationIdAndIsArchivedAndSearch(
      @Param("organizationId") UUID organizationId,
      @Param("isArchived") boolean isArchived,
      @Param("search") String search,
      Pageable pageable);

  @Query(
      """
      SELECT DISTINCT p FROM Product p
      LEFT JOIN FETCH p.category
      WHERE p.id IN :ids
      """)
  List<Product> findAllByIdIn(@Param("ids") List<UUID> ids);

  @Query(
      """
      SELECT DISTINCT p FROM Product p
      LEFT JOIN FETCH p.category
      WHERE p.id IN :ids AND p.organization.id = :organizationId
      """)
  List<Product> findAllByIdInAndOrganizationId(
      @Param("ids") List<UUID> ids, @Param("organizationId") UUID organizationId);

  @Query(
      """
      SELECT p FROM Product p
      LEFT JOIN FETCH p.organization
      LEFT JOIN FETCH p.category
      LEFT JOIN FETCH p.createdBy
      LEFT JOIN FETCH p.updatedBy
      WHERE p.id = :id AND p.organization.id = :organizationId
      """)
  Optional<Product> findByIdAndOrganizationId(UUID id, UUID organizationId);

  @Query("""
      SELECT p FROM Product p
      WHERE p.organization.id = :organizationId
      """)
  List<Product> findAllByOrganizationId(@Param("organizationId") UUID organizationId);

  boolean existsByOrganizationIdAndSkuIgnoreCase(UUID organizationId, String sku);

  boolean existsByOrganizationIdAndSkuIgnoreCaseAndIdNot(UUID organizationId, String sku, UUID id);
}
