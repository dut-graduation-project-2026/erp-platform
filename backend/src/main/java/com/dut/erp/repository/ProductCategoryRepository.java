package com.dut.erp.repository;

import com.dut.erp.entity.ProductCategory;
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
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, UUID> {
  @Query("SELECT pc.id FROM ProductCategory pc WHERE pc.organization.id = :organizationId")
  Page<UUID> findIdsByOrganizationId(@Param("organizationId") UUID organizationId, Pageable pageable);

  @Query("""
      SELECT pc.id FROM ProductCategory pc
      WHERE pc.organization.id = :organizationId
        AND LOWER(pc.name) LIKE LOWER(CONCAT('%', :search, '%'))
      """)
  Page<UUID> findIdsByOrganizationIdAndSearch(
      @Param("organizationId") UUID organizationId,
      @Param("search") String search,
      Pageable pageable);

  @Query("SELECT DISTINCT pc FROM ProductCategory pc LEFT JOIN FETCH pc.organization WHERE pc.id IN :ids")
  List<ProductCategory> findAllByIdIn(@Param("ids") List<UUID> ids);

  @Query("""
      SELECT pc FROM ProductCategory pc
      LEFT JOIN FETCH pc.organization
      LEFT JOIN FETCH pc.createdBy
      LEFT JOIN FETCH pc.updatedBy
      WHERE pc.id = :id AND pc.organization.id = :organizationId
      """)
  Optional<ProductCategory> findByIdAndOrganizationId(UUID id, UUID organizationId);

  boolean existsByOrganizationIdAndName(UUID organizationId, String name);
  
  boolean existsByOrganizationIdAndNameAndIdNot(UUID organizationId, String name, UUID id);
  
  @Query("SELECT count(p) > 0 FROM Product p WHERE p.category.id = :categoryId")
  boolean hasProductsLinked(@Param("categoryId") UUID categoryId);
}
