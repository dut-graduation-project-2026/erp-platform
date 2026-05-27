package com.dut.erp.repository;

import com.dut.erp.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
  @Query(
      """
        SELECT DISTINCT u
        FROM User u
        LEFT JOIN FETCH u.roles r
        LEFT JOIN FETCH u.organizations o
        WHERE u.id = :userId
      """)
  Optional<User> findByIdWithRolesAndOrganizations(@Param("userId") UUID userId);

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  Optional<User> findByIdAndOrganizationsId(UUID id, UUID organizationId);

  Page<User> findAllByOrganizationsId(UUID organizationId, Pageable pageable);

  @Query(
      value =
          """
            SELECT DISTINCT u
            FROM User u
            JOIN u.organizations o
            WHERE o.id = :organizationId
              AND (
                LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) ESCAPE '\\'
                OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) ESCAPE '\\'
                OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')) ESCAPE '\\'
                OR LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :query, '%')) ESCAPE '\\'
                OR LOWER(CONCAT(u.lastName, ' ', u.firstName)) LIKE LOWER(CONCAT('%', :query, '%')) ESCAPE '\\'
              )
          """,
      countQuery =
          """
            SELECT COUNT(DISTINCT u.id)
            FROM User u
            JOIN u.organizations o
            WHERE o.id = :organizationId
              AND (
                LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) ESCAPE '\\'
                OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) ESCAPE '\\'
                OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')) ESCAPE '\\'
                OR LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :query, '%')) ESCAPE '\\'
                OR LOWER(CONCAT(u.lastName, ' ', u.firstName)) LIKE LOWER(CONCAT('%', :query, '%')) ESCAPE '\\'
              )
          """)
  Page<User> searchByOrganizationsIdAndQuery(
      @Param("organizationId") UUID organizationId,
      @Param("query") String query,
      Pageable pageable);
}
