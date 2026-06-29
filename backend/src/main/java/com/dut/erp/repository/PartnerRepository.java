package com.dut.erp.repository;

import com.dut.erp.entity.Partner;
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
public interface PartnerRepository extends JpaRepository<Partner, UUID> {

  @Query(
      """
      SELECT DISTINCT p FROM Partner p
      LEFT JOIN FETCH p.contacts
      WHERE p.organization.id = :organizationId
      """)
  List<Partner> findAllByOrganizationId(@Param("organizationId") UUID organizationId);

  @Query(
      """
      SELECT p FROM Partner p
      LEFT JOIN FETCH p.contacts
      WHERE p.id = :id
      AND p.organization.id = :organizationId
      """)
  Optional<Partner> findByIdAndOrganizationId(
      @Param("id") UUID id, @Param("organizationId") UUID organizationId);

  @Query(
      """
      SELECT p.id FROM Partner p
      WHERE p.organization.id = :organizationId
        AND (:search IS NULL OR :search = ''
             OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
             OR LOWER(p.taxCode) LIKE LOWER(CONCAT('%', :search, '%'))
             OR LOWER(p.email) LIKE LOWER(CONCAT('%', :search, '%'))
             OR LOWER(p.phone) LIKE LOWER(CONCAT('%', :search, '%')))
      """)
  Page<UUID> findPartnerIdsByOrganizationIdAndSearch(
      @Param("organizationId") UUID organizationId,
      @Param("search") String search,
      Pageable pageable);

  @Query(
      """
      SELECT p.id FROM Partner p
      WHERE p.organization.id = :organizationId
      """)
  Page<UUID> findPartnerIdsByOrganizationId(
      @Param("organizationId") UUID organizationId,
      Pageable pageable);

  @Query(
      """
      SELECT DISTINCT p FROM Partner p
      LEFT JOIN FETCH p.contacts
      WHERE p.id IN :ids
      """)
  List<Partner> findAllByIdIn(@Param("ids") List<UUID> ids);
}
