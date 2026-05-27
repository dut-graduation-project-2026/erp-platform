package com.dut.erp.repository;

import com.dut.erp.entity.SalePartner;
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
public interface SalePartnerRepository extends JpaRepository<SalePartner, UUID> {

  Optional<SalePartner> findByCodeAndOrganizationId(String code, UUID organizationId);

  @Query("""
      SELECT sp.id
      FROM SalePartner sp
      WHERE sp.organization.id = :organizationId
      """)
  Page<UUID> findIdsByOrganizationId(@Param("organizationId") UUID organizationId, Pageable pageable);

  @Query("""
      SELECT DISTINCT sp FROM SalePartner sp
      LEFT JOIN FETCH sp.organization
      WHERE sp.id IN :ids
      """)
  List<SalePartner> findAllByIdIn(@Param("ids") List<UUID> ids);

  @Query("""
      SELECT sp FROM SalePartner sp
      LEFT JOIN FETCH sp.organization
      LEFT JOIN FETCH sp.contacts
      WHERE sp.id = :id
      """)
  Optional<SalePartner> findByIdWithContacts(@Param("id") UUID id);
}
