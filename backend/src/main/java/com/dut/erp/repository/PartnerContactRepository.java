package com.dut.erp.repository;

import com.dut.erp.entity.PartnerContact;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PartnerContactRepository extends JpaRepository<PartnerContact, UUID> {

  @Query(
      """
      SELECT pc FROM PartnerContact pc
      WHERE pc.partner.id = :partnerId
      """)
  List<PartnerContact> findAllByPartnerId(@Param("partnerId") UUID partnerId);

  @Query(
      """
      SELECT pc FROM PartnerContact pc
      WHERE pc.id = :contactId
      AND pc.partner.id = :partnerId
      """)
  Optional<PartnerContact> findByIdAndPartnerId(
      @Param("contactId") UUID contactId, @Param("partnerId") UUID partnerId);
}
