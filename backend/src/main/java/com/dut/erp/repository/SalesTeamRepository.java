package com.dut.erp.repository;

import com.dut.erp.entity.SalesTeam;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesTeamRepository extends JpaRepository<SalesTeam, UUID> {

  @Query("""
      SELECT st FROM SalesTeam st
      LEFT JOIN FETCH st.organization
      LEFT JOIN FETCH st.leader
      WHERE st.organization.id = :organizationId
      ORDER BY st.name ASC
      """)
  List<SalesTeam> findAllByOrganizationId(@Param("organizationId") UUID organizationId);
}
