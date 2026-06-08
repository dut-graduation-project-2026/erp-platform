package com.dut.erp.repository;

import com.dut.erp.entity.Tax;
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
public interface TaxRepository extends JpaRepository<Tax, UUID> {

  @Query(
      """
      SELECT t.id
      FROM Tax t
      WHERE t.organization.id = :organizationId AND t.isArchived = :isArchived
      """)
  Page<UUID> findIdsByOrganizationIdAndIsArchived(
      @Param("organizationId") UUID organizationId,
      @Param("isArchived") boolean isArchived,
      Pageable pageable);

  @Query(
      """
      SELECT t.id
      FROM Tax t
      WHERE t.organization.id = :organizationId
      AND t.isArchived = :isArchived
      AND LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%'))
      """)
  Page<UUID> findIdsByOrganizationIdAndIsArchivedAndSearch(
      @Param("organizationId") UUID organizationId,
      @Param("isArchived") boolean isArchived,
      @Param("search") String search,
      Pageable pageable);

  @Query(
      """
      SELECT DISTINCT t FROM Tax t
      LEFT JOIN FETCH t.organization
      LEFT JOIN FETCH t.createdBy
      LEFT JOIN FETCH t.updatedBy
      WHERE t.id IN :ids
      """)
  List<Tax> findAllByIdIn(@Param("ids") List<UUID> ids);

  @Query(
      """
      SELECT t FROM Tax t
      LEFT JOIN FETCH t.organization
      LEFT JOIN FETCH t.createdBy
      LEFT JOIN FETCH t.updatedBy
      WHERE t.id = :id AND t.organization.id = :organizationId
      """)
  Optional<Tax> findByIdAndOrganizationId(UUID id, UUID organizationId);
}
