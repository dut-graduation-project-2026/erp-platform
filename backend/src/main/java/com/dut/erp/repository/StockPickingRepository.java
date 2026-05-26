package com.dut.erp.repository;

import com.dut.erp.entity.StockPicking;
import com.dut.erp.enums.PickingType;
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
public interface StockPickingRepository extends JpaRepository<StockPicking, UUID> {

  Optional<StockPicking> findByIdAndOrganizationId(UUID id, UUID organizationId);

  long countByPickingTypeAndOrganizationId(PickingType pickingType, UUID organizationId);

  @Query("""
      SELECT sp.id
      FROM StockPicking sp
      WHERE sp.organization.id = :organizationId
        AND (:pickingType IS NULL OR sp.pickingType = :pickingType)
      """)
  Page<UUID> findIdsByOrganizationIdAndPickingType(
      @Param("organizationId") UUID organizationId,
      @Param("pickingType") PickingType pickingType,
      Pageable pageable
  );

  @Query("""
      SELECT DISTINCT sp FROM StockPicking sp
      LEFT JOIN FETCH sp.organization
      LEFT JOIN FETCH sp.location
      LEFT JOIN FETCH sp.locationDest
      LEFT JOIN FETCH sp.partner
      LEFT JOIN FETCH sp.saleOrder
      WHERE sp.id IN :ids
      """)
  List<StockPicking> findAllByIdIn(@Param("ids") List<UUID> ids);
}
