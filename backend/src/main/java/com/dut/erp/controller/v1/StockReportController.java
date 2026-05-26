package com.dut.erp.controller.v1;

import com.dut.erp.dto.response.StockXntReportLine;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.StockReportService;
import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for inventory reports and Excel/PDF document exports.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/reports/stock")
public class StockReportController {

  private final StockReportService stockReportService;

  /**
   * Retrieves the Stock XNT (Beginning-Inbound-Outbound-Ending) report data.
   */
  @GetMapping("/xnt")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and @securityAuthService.hasPermission('quants:select', #organizationId, #userDetails)
      """)
  public ResponseEntity<List<StockXntReportLine>> getStockXntReport(
      @PathVariable UUID organizationId,
      @RequestParam Instant startDate,
      @RequestParam Instant endDate,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(stockReportService.getXntReport(organizationId, startDate, endDate));
  }

  /**
   * Exports the Stock XNT report to an Excel spreadsheet.
   */
  @GetMapping("/xnt/excel")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and @securityAuthService.hasPermission('quants:select', #organizationId, #userDetails)
      """)
  public ResponseEntity<InputStreamResource> exportStockXntExcel(
      @PathVariable UUID organizationId,
      @RequestParam Instant startDate,
      @RequestParam Instant endDate,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    ByteArrayInputStream in = stockReportService.exportXntToExcel(organizationId, startDate, endDate);
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Disposition", "attachment; filename=stock_xnt_report.xlsx");

    return ResponseEntity.ok()
        .headers(headers)
        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .body(new InputStreamResource(in));
  }

  /**
   * Exports a specific Stock Picking record to a PDF document.
   */
  @GetMapping("/pickings/{pickingId}/pdf")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and @securityAuthService.hasPermission('pickings:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<InputStreamResource> exportPickingPdf(
      @PathVariable UUID organizationId,
      @PathVariable UUID pickingId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    ByteArrayInputStream in = stockReportService.exportPickingToPdf(organizationId, pickingId);
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Disposition", "attachment; filename=picking_" + pickingId + ".pdf");

    return ResponseEntity.ok()
        .headers(headers)
        .contentType(MediaType.APPLICATION_PDF)
        .body(new InputStreamResource(in));
  }

  /**
   * Exports a specific Stock Inventory Stocktake record to a PDF document.
   */
  @GetMapping("/inventories/{inventoryId}/pdf")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and @securityAuthService.hasPermission('inventory:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<InputStreamResource> exportInventoryPdf(
      @PathVariable UUID organizationId,
      @PathVariable UUID inventoryId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    ByteArrayInputStream in = stockReportService.exportInventoryToPdf(organizationId, inventoryId);
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Disposition", "attachment; filename=stocktake_" + inventoryId + ".pdf");

    return ResponseEntity.ok()
        .headers(headers)
        .contentType(MediaType.APPLICATION_PDF)
        .body(new InputStreamResource(in));
  }
}
