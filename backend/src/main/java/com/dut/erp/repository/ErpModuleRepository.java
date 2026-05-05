package com.dut.erp.repository;

import com.dut.erp.entity.ErpModule;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ErpModuleRepository extends JpaRepository<ErpModule, UUID> {
  @Query(
      """
          SELECT DISTINCT p.module
          FROM User u
          JOIN u.roles r
          JOIN r.permissions p
          JOIN u.organizations o
          WHERE u.id = :userId
            AND o.id = :organizationId
            AND r.organization.id = :organizationId
      """)
  List<ErpModule> findAccessibleModulesByUserAndOrganization(
      @Param("userId") UUID userId, @Param("organizationId") UUID organizationId);

  @Query(
      """
          SELECT DISTINCT p.module
          FROM Role r
          JOIN r.permissions p
          WHERE r.organization.id = :organizationId
      """)
  List<ErpModule> findByOrganizationIdWithPermissions(@Param("organizationId") UUID organizationId);
}
