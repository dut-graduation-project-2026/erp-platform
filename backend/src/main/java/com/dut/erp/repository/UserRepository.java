package com.dut.erp.repository;

import com.dut.erp.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {
  @Query(
      """
        SELECT DISTINCT u
        FROM User u
        LEFT JOIN FETCH u.roles r
        LEFT JOIN FETCH u.organizations o
        WHERE u.id = :id
      """)
  Optional<User> findByIdWithRolesAndOrganizations(@Param("id") UUID userId);

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  @Query(
      """
        SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END
        FROM User u
        JOIN u.organizations o
        WHERE u.id = :userId AND o.id = :organizationId
      """)
  boolean existsByIdAndOrganizations_Id(
      @Param("userId") UUID userId, @Param("organizationId") UUID organizationId);

  Page<User> findAllByOrganizations_Id(UUID organizationId, Pageable pageable);

  Page<User> findAllByEmailContainingIgnoreCase(String email, Pageable pageable);
}
