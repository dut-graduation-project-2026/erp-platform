package com.dut.erp.repository;

import com.dut.erp.entity.RefreshToken;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
  @Transactional
  @Modifying
  @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
  int deleteExpiredTokens(@Param("now") Instant now);

  @Transactional
  @Modifying
  @Query("DELETE FROM RefreshToken rt WHERE rt.jti = :jti")
  int deleteByJti(@Param("jti") String jti);
}
