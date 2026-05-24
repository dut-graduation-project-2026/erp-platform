package com.dut.erp.repository;

import com.dut.erp.entity.Organization;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
  boolean existsByTaxCode(String taxCode);

  @Query(
      """
      SELECT DISTINCT o
      FROM Organization o
      JOIN o.users u
      WHERE u.id = :userId
      """)
  List<Organization> findAllByUserId(@Param("userId") UUID userId);

  @Query(
      """
      SELECT o
      FROM Organization o
      LEFT JOIN FETCH o.users
      WHERE o.id = :organizationId
      """)
  Optional<Organization> findByIdWithUsers(@Param("organizationId") UUID organizationId);
}
