package com.dut.erp.scheduler;

import com.dut.erp.dto.response.StockAlertResponse;
import com.dut.erp.entity.Organization;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.service.StockAlertService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertScheduler {

  private final OrganizationRepository organizationRepository;
  private final StockAlertService stockAlertService;

  /**
   * Periodically scans all organizations for minimum stock and expiring lot alerts.
   * Runs daily at 1:00 AM.
   */
  @Scheduled(cron = "0 0 1 * * *")
  public void scanAndLogStockAlerts() {
    log.info("Starting scheduled scan for stock alerts...");
    List<Organization> organizations = organizationRepository.findAll();
    for (Organization org : organizations) {
      try {
        List<StockAlertResponse> alerts = stockAlertService.getAlerts(org.getId());
        if (!alerts.isEmpty()) {
          log.warn("Organization {} ({}) has {} active stock alerts:", org.getName(), org.getId(), alerts.size());
          for (StockAlertResponse alert : alerts) {
            log.warn("  - [{}] {}", alert.type(), alert.message());
          }
        }
      } catch (Exception e) {
        log.error("Failed to scan stock alerts for organization: " + org.getId(), e);
      }
    }
    log.info("Scheduled scan for stock alerts completed.");
  }
}
