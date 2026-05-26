package com.dut.erp.repository;

import com.dut.erp.entity.ProductTemplate;
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
public interface ProductTemplateRepository extends JpaRepository<ProductTemplate, UUID> {

  Optional<ProductTemplate> findByIdAndOrganizationId(UUID id, UUID organizationId);

  @Query("""
      SELECT pt.id
      FROM ProductTemplate pt
      WHERE pt.organization.id = :organizationId
      """)
  Page<UUID> findIdsByOrganizationId(@Param("organizationId") UUID organizationId, Pageable pageable);

  @Query("""
      SELECT DISTINCT pt FROM ProductTemplate pt
      LEFT JOIN FETCH pt.organization
      WHERE pt.id IN :ids
      """)
  List<ProductTemplate> findAllByIdIn(@Param("ids") List<UUID> ids);
}
