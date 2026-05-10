package com.dut.erp.scheduler;

import com.dut.erp.repository.OrganizationInvitationRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvitationCheckerScheduler {
  private final OrganizationInvitationRepository organizationInvitationRepository;

  @Scheduled(cron = "0 0 0 * * *")
  @Transactional
  public void updateInvitationStatuses() {
    Instant now = Instant.now();
    int updatedInvitations = organizationInvitationRepository.updateExpiredInvitations(now);
    log.info("Invitation status update completed: {} invitations updated", updatedInvitations);
  }

  @Scheduled(cron = "0 0 0 * * SUN")
  @Transactional
  public void cleanupExpiredInvitations() {
    Instant now = Instant.now();
    int deletedInvitations = organizationInvitationRepository.deleteExpiredInvitations(now);
    log.info("Invitation cleanup completed: {} expired invitations removed", deletedInvitations);
  }
}
