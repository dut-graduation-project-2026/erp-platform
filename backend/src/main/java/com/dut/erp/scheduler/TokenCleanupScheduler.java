package com.dut.erp.scheduler;

import com.dut.erp.repository.InvalidatedAccessTokenRepository;
import com.dut.erp.repository.RefreshTokenRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {
  private final RefreshTokenRepository refreshTokenRepository;
  private final InvalidatedAccessTokenRepository invalidatedAccessTokenRepository;

  @Scheduled(cron = "0 0 2 * * SUN")
  @Transactional
  public void cleanupExpiredRefreshTokens() {
    Instant now = Instant.now();
    int deletedRefreshTokens = refreshTokenRepository.deleteExpiredTokens(now);
    log.info("Token cleanup completed: {} refresh tokens", deletedRefreshTokens);
  }

  @Scheduled(cron = "0 0 0 * * *")
  @Transactional
  public void cleanupExpiredAccessTokens() {
    Instant now = Instant.now();
    int deletedAccessTokens = invalidatedAccessTokenRepository.deleteExpiredTokens(now);
    log.info("Token cleanup completed: {} invalidated access tokens removed", deletedAccessTokens);
  }
}
