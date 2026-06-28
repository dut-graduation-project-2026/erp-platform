package com.dut.erp.scheduler;

import com.dut.erp.entity.Organization;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.service.AiService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisScheduler {

  private final OrganizationRepository organizationRepository;
  private final AiService aiService;

  /**
   * Run AI analysis pre-calculations for all organizations at 00:05 AM Asia/Ho_Chi_Minh timezone every day.
   */
  @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Ho_Chi_Minh")
  public void runDailyAnalysis() {
    log.info("Starting daily scheduled AI analysis pre-calculations");
    
    List<Organization> organizations = organizationRepository.findAll();
    for (Organization org : organizations) {
      try {
        log.info("Running AI analysis pre-calculations for organization: {}", org.getId());
        
        // 1. Sales Forecast
        aiService.getSalesForecast(org.getId(), "30d");
        
        // 2. Inventory Analysis (Force recalculation at start of day)
        aiService.getInventoryAnalysis(org.getId(), true);
        
        // 3. Inventory Alerts
        aiService.getInventoryAlerts(org.getId());
        
        // 4. Reorder Recommendations
        aiService.getReorderRecommendations(org.getId());
        
        // 5. Dashboard Summary
        aiService.getDashboardSummary(org.getId());
        
        log.info("Successfully completed pre-calculations for organization: {}", org.getId());
      } catch (Exception e) {
        log.error("Failed to run daily scheduled AI analysis for organization: {}", org.getId(), e);
      }
    }
    
    log.info("Completed daily scheduled AI analysis pre-calculations");
  }
}
