package com.dut.erp.repository;

import com.dut.erp.entity.PartnerContact;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartnerContactRepository extends JpaRepository<PartnerContact, UUID> {

  List<PartnerContact> findAllByPartnerId(UUID partnerId);
}
