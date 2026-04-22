package com.dut.erp.repository;

import com.dut.erp.entity.InvalidatedAccessToken;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface InvalidatedAccessTokenRepository
    extends JpaRepository<InvalidatedAccessToken, String> {
  @Transactional
  @Modifying
  @Query("DELETE FROM InvalidatedAccessToken t WHERE t.expiresAt < :now")
  int deleteExpiredTokens(@Param("now") Instant now);

  @Transactional
  @Modifying
  @Query(
      value =
          "INSERT INTO invalidated_access_tokens (jti, expires_at) "
              + "VALUES (:jti, :expiresAt) "
              + "ON CONFLICT (jti) DO NOTHING",
      nativeQuery = true)
  int insertIgnoreJti(@Param("jti") String jti, @Param("expiresAt") Instant expiresAt);
}
