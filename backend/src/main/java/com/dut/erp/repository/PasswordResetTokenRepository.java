package com.dut.erp.repository;

import com.dut.erp.entity.PasswordResetToken;
import com.dut.erp.entity.User;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
  Optional<PasswordResetToken> findByToken(String token);

  @Transactional
  @Modifying
  @Query("DELETE FROM PasswordResetToken prt WHERE prt.user = :user")
  void deleteByUser(@Param("user") User user);

  @Transactional
  @Modifying
  @Query("DELETE FROM PasswordResetToken prt WHERE prt.expiresAt < :now")
  int deleteExpiredTokens(@Param("now") Instant now);
}
