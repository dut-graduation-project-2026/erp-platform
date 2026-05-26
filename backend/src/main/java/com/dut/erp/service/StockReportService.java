package com.dut.erp.service;

import com.dut.erp.dto.response.StockXntReportLine;
import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface StockReportService {
  List<StockXntReportLine> getXntReport(UUID organizationId, Instant startDate, Instant endDate);
  ByteArrayInputStream exportXntToExcel(UUID organizationId, Instant startDate, Instant endDate);
  ByteArrayInputStream exportPickingToPdf(UUID organizationId, UUID pickingId);
  ByteArrayInputStream exportInventoryToPdf(UUID organizationId, UUID inventoryId);
}
