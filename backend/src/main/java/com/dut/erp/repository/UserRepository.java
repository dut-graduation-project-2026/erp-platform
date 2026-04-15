package com.dut.erp.repository;

import com.dut.erp.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

  @Query(
      """
          SELECT DISTINCT u
          FROM User u
          LEFT JOIN FETCH u.roles r
          LEFT JOIN FETCH r.permissions p
          LEFT JOIN FETCH p.actions
          WHERE u.id = :id
      """)
  Optional<User> findByIdWithFullRoles(@Param("id") UUID userId);

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);
}
