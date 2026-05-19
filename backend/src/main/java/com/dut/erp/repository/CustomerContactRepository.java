package com.dut.erp.repository;

import com.dut.erp.entity.CustomerContact;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerContactRepository extends JpaRepository<CustomerContact, UUID> {
  Optional<CustomerContact> findByIdAndCustomerId(UUID id, UUID customerId);
}
