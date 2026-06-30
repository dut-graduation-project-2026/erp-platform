package com.dut.erp.repository;

import com.dut.erp.entity.SaleTeam;
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
public interface SaleTeamRepository extends JpaRepository<SaleTeam, UUID> {

  @Query(
      """
      SELECT s.id
      FROM SaleTeam s
      WHERE s.organization.id = :organizationId AND s.isArchived = :isArchived
      """)
  Page<UUID> findIdsByOrganizationIdAndIsArchived(
      @Param("organizationId") UUID organizationId,
      @Param("isArchived") boolean isArchived,
      Pageable pageable);

  @Query(
      """
      SELECT s.id
      FROM SaleTeam s
      WHERE s.organization.id = :organizationId
      AND s.isArchived = :isArchived
      AND LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))
      """)
  Page<UUID> findIdsByOrganizationIdAndIsArchivedAndSearch(
      @Param("organizationId") UUID organizationId,
      @Param("isArchived") boolean isArchived,
      @Param("search") String search,
      Pageable pageable);

  @Query(
      """
      SELECT DISTINCT s FROM SaleTeam s
      LEFT JOIN FETCH s.leader
      LEFT JOIN FETCH s.organization
      LEFT JOIN FETCH s.members
      WHERE s.id IN :ids
      """)
  List<SaleTeam> findAllByIdIn(@Param("ids") List<UUID> ids);

  @Query(
      """
      SELECT s FROM SaleTeam s
      LEFT JOIN FETCH s.leader
      LEFT JOIN FETCH s.organization
      LEFT JOIN FETCH s.members
      WHERE s.id = :id AND s.organization.id = :organizationId
      """)
  Optional<SaleTeam> findByIdAndOrganizationId(
      @Param("id") UUID id,
      @Param("organizationId") UUID organizationId);

  @Query(
      """
      SELECT DISTINCT s FROM SaleTeam s
      LEFT JOIN FETCH s.leader
      LEFT JOIN FETCH s.organization
      LEFT JOIN FETCH s.members
      WHERE s.organization.id = :organizationId
      AND :userId IN (SELECT m.id FROM s.members m)
      """)
  List<SaleTeam> findAllByOrganizationIdAndMemberId(
      @Param("organizationId") UUID organizationId,
      @Param("userId") UUID userId);

  @Query(
      """
      SELECT s.id
      FROM SaleTeam s
      WHERE s.organization.id = :organizationId
      AND (s.leader.id = :userId OR :userId IN (SELECT m.id FROM s.members m))
      """)
  List<UUID> findIdsByOrganizationIdAndUserId(
      @Param("organizationId") UUID organizationId,
      @Param("userId") UUID userId);

  boolean existsByOrganizationIdAndName(UUID organizationId, String name);

  boolean existsByOrganizationIdAndNameAndIdNot(UUID organizationId, String name, UUID id);
}
