package com.dut.erp.repository;

import com.dut.erp.entity.Partner;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
}
