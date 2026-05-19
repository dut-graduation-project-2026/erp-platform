package com.dut.erp.repository;

import com.dut.erp.entity.Customer;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
  Optional<Customer> findByCodeAndOrganizationId(String code, UUID organizationId);

  Page<Customer> findAllByOrganizationId(UUID organizationId, Pageable pageable);

  @Query(
      value =
          """
            SELECT c
            FROM Customer c
            WHERE c.organization.id = :organizationId
              AND (
                LOWER(c.code) LIKE LOWER(CONCAT('%', :query, '%')) ESCAPE '\\'
                OR LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) ESCAPE '\\'
                OR LOWER(c.phone) LIKE LOWER(CONCAT('%', :query, '%')) ESCAPE '\\'
                OR LOWER(c.email) LIKE LOWER(CONCAT('%', :query, '%')) ESCAPE '\\'
              )
          """,
      countQuery =
          """
            SELECT COUNT(c)
            FROM Customer c
            WHERE c.organization.id = :organizationId
              AND (
                LOWER(c.code) LIKE LOWER(CONCAT('%', :query, '%')) ESCAPE '\\'
                OR LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) ESCAPE '\\'
                OR LOWER(c.phone) LIKE LOWER(CONCAT('%', :query, '%')) ESCAPE '\\'
                OR LOWER(c.email) LIKE LOWER(CONCAT('%', :query, '%')) ESCAPE '\\'
              )
          """)
  Page<Customer> searchByOrganizationIdAndQuery(
      @Param("organizationId") UUID organizationId,
      @Param("query") String query,
      Pageable pageable);

  @Query(
      """
        SELECT c FROM Customer c
        LEFT JOIN FETCH c.contacts
        WHERE c.id = :id AND c.organization.id = :organizationId
      """)
  Optional<Customer> findByIdAndOrganizationIdWithContacts(
      @Param("id") UUID id, @Param("organizationId") UUID organizationId);
}
