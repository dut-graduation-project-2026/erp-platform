package com.dut.erp.service.impl;

import com.dut.erp.dto.response.StockXntReportLine;
import com.dut.erp.entity.Product;
import com.dut.erp.entity.StockInventory;
import com.dut.erp.entity.StockInventoryLine;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.dut.erp.entity.StockMove;
import com.dut.erp.entity.StockPicking;
import com.dut.erp.entity.StockValuation;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.repository.ProductRepository;
import com.dut.erp.repository.StockInventoryRepository;
import com.dut.erp.repository.StockPickingRepository;
import com.dut.erp.repository.StockValuationRepository;
import com.dut.erp.service.StockReportService;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockReportServiceImpl implements StockReportService {

  private final OrganizationRepository organizationRepository;
  private final ProductRepository productRepository;
  private final StockValuationRepository stockValuationRepository;
  private final StockPickingRepository stockPickingRepository;
  private final StockInventoryRepository stockInventoryRepository;

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
      .ofPattern("yyyy-MM-dd HH:mm:ss")
      .withZone(ZoneId.systemDefault());

  @Override
  public List<StockXntReportLine> getXntReport(UUID organizationId, Instant startDate, Instant endDate) {
    log.info("Generating XNT report for organization {} between {} and {}", organizationId, startDate, endDate);

    if (!organizationRepository.existsById(organizationId)) {
      throw new ResourceNotFoundException("Organization not found with id: " + organizationId);
    }

    List<Product> products = productRepository.findAllByOrganizationId(organizationId);
    List<StockXntReportLine> reportLines = new ArrayList<>();

    for (Product product : products) {
      List<StockValuation> valuations = stockValuationRepository.findAllByProductIdOrderByCreatedAtDesc(product.getId());
      valuations.sort(Comparator.comparing(StockValuation::getCreatedAt));

      BigDecimal beginningQty = BigDecimal.ZERO;
      BigDecimal beginningValue = BigDecimal.ZERO;
      BigDecimal inboundQty = BigDecimal.ZERO;
      BigDecimal inboundValue = BigDecimal.ZERO;
      BigDecimal outboundQty = BigDecimal.ZERO;
      BigDecimal outboundValue = BigDecimal.ZERO;
      BigDecimal endingQty = BigDecimal.ZERO;
      BigDecimal endingValue = BigDecimal.ZERO;

      for (StockValuation val : valuations) {
        Instant created = val.getCreatedAt();
        BigDecimal qty = val.getQuantity();
        BigDecimal value = val.getTotalValue();

        if (created.isBefore(startDate)) {
          beginningQty = beginningQty.add(qty);
          beginningValue = beginningValue.add(value);
        }

        if (!created.isBefore(startDate) && !created.isAfter(endDate)) {
          if (qty.compareTo(BigDecimal.ZERO) > 0) {
            inboundQty = inboundQty.add(qty);
            inboundValue = inboundValue.add(value);
          } else {
            outboundQty = outboundQty.add(qty.abs());
            outboundValue = outboundValue.add(value.abs());
          }
        }

        if (!created.isAfter(endDate)) {
          endingQty = endingQty.add(qty);
          endingValue = endingValue.add(value);
        }
      }

      reportLines.add(new StockXntReportLine(
          product.getId(),
          product.getSku(),
          product.getName(),
          beginningQty,
          beginningValue,
          inboundQty,
          inboundValue,
          outboundQty,
          outboundValue,
          endingQty,
          endingValue
      ));
    }

    return reportLines;
  }

  @Override
  public ByteArrayInputStream exportXntToExcel(UUID organizationId, Instant startDate, Instant endDate) {
    List<StockXntReportLine> lines = getXntReport(organizationId, startDate, endDate);

    try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet("Stock XNT Report");

      // Header row
      Row headerRow = sheet.createRow(0);
      String[] columns = {
          "Product SKU", "Product Name", 
          "Beginning Qty", "Beginning Value", 
          "Inbound Qty", "Inbound Value", 
          "Outbound Qty", "Outbound Value", 
          "Ending Qty", "Ending Value"
      };

      // Header style
      CellStyle headerCellStyle = workbook.createCellStyle();
      headerCellStyle.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
      headerCellStyle.setFillPattern(FillPatternType.FINE_DOTS);
      org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
      headerFont.setBold(true);
      headerCellStyle.setFont(headerFont);

      for (int i = 0; i < columns.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(columns[i]);
        cell.setCellStyle(headerCellStyle);
      }

      int rowIdx = 1;
      for (StockXntReportLine line : lines) {
        Row row = sheet.createRow(rowIdx++);

        row.createCell(0).setCellValue(line.sku());
        row.createCell(1).setCellValue(line.name());
        row.createCell(2).setCellValue(line.beginningQty().doubleValue());
        row.createCell(3).setCellValue(line.beginningValue().doubleValue());
        row.createCell(4).setCellValue(line.inboundQty().doubleValue());
        row.createCell(5).setCellValue(line.inboundValue().doubleValue());
        row.createCell(6).setCellValue(line.outboundQty().doubleValue());
        row.createCell(7).setCellValue(line.outboundValue().doubleValue());
        row.createCell(8).setCellValue(line.endingQty().doubleValue());
        row.createCell(9).setCellValue(line.endingValue().doubleValue());
      }

      for (int i = 0; i < columns.length; i++) {
        sheet.autoSizeColumn(i);
      }

      workbook.write(out);
      return new ByteArrayInputStream(out.toByteArray());

    } catch (IOException e) {
      log.error("Failed to generate Excel report", e);
      throw new RuntimeException("Excel generation failed", e);
    }
  }

  @Override
  public ByteArrayInputStream exportPickingToPdf(UUID organizationId, UUID pickingId) {
    log.info("Exporting picking {} to PDF for organization {}", pickingId, organizationId);
    StockPicking picking = stockPickingRepository.findById(pickingId)
        .orElseThrow(() -> new ResourceNotFoundException("Stock picking not found with id: " + pickingId));

    if (!picking.getOrganization().getId().equals(organizationId)) {
      throw new IllegalArgumentException("Stock picking does not belong to organization: " + organizationId);
    }

    Document document = new Document();
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    try {
      PdfWriter.getInstance(document, out);
      document.open();

      // Title
      Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
      Paragraph title = new Paragraph("STOCK OPERATION RECORD", titleFont);
      title.setAlignment(Element.ALIGN_CENTER);
      document.add(title);
      document.add(new Paragraph(" "));

      // Metadata
      Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
      document.add(new Paragraph("Reference: " + picking.getName(), metaFont));
      document.add(new Paragraph("Operation Type: " + picking.getPickingType(), metaFont));
      document.add(new Paragraph("State: " + picking.getState(), metaFont));
      document.add(new Paragraph("Source Location: " + picking.getLocation().getCode(), metaFont));
      document.add(new Paragraph("Dest Location: " + picking.getLocationDest().getCode(), metaFont));
      if (picking.getPartner() != null) {
        document.add(new Paragraph("Partner: " + picking.getPartner().getName(), metaFont));
      }
      if (picking.getScheduledDate() != null) {
        document.add(new Paragraph("Scheduled Date: " + DATE_FORMATTER.format(picking.getScheduledDate()), metaFont));
      }
      if (picking.getDateDone() != null) {
        document.add(new Paragraph("Execution Date: " + DATE_FORMATTER.format(picking.getDateDone()), metaFont));
      }
      document.add(new Paragraph(" "));

      // Table of Moves
      PdfPTable table = new PdfPTable(4);
      table.setWidthPercentage(100);
      table.setWidths(new float[]{2f, 4f, 2f, 2f});

      // Headers
      PdfPCell cell = new PdfPCell(new Phrase("Product SKU", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
      cell.setBackgroundColor(Color.LIGHT_GRAY);
      cell.setHorizontalAlignment(Element.ALIGN_CENTER);
      table.addCell(cell);

      cell = new PdfPCell(new Phrase("Product Name", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
      cell.setBackgroundColor(Color.LIGHT_GRAY);
      cell.setHorizontalAlignment(Element.ALIGN_CENTER);
      table.addCell(cell);

      cell = new PdfPCell(new Phrase("Scheduled Qty", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
      cell.setBackgroundColor(Color.LIGHT_GRAY);
      cell.setHorizontalAlignment(Element.ALIGN_CENTER);
      table.addCell(cell);

      cell = new PdfPCell(new Phrase("Done Qty", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
      cell.setBackgroundColor(Color.LIGHT_GRAY);
      cell.setHorizontalAlignment(Element.ALIGN_CENTER);
      table.addCell(cell);

      // Data Rows
      for (StockMove move : picking.getStockMoves()) {
        table.addCell(new Phrase(move.getProduct().getSku(), metaFont));
        table.addCell(new Phrase(move.getProduct().getName(), metaFont));
        table.addCell(new Phrase(move.getProductUomQty().toString(), metaFont));
        table.addCell(new Phrase(move.getQuantityDone().toString(), metaFont));
      }

      document.add(table);
      document.close();

    } catch (DocumentException e) {
      log.error("Failed to generate PDF for picking", e);
      throw new RuntimeException("PDF generation failed", e);
    }

    return new ByteArrayInputStream(out.toByteArray());
  }

  @Override
  public ByteArrayInputStream exportInventoryToPdf(UUID organizationId, UUID inventoryId) {
    log.info("Exporting inventory audit {} to PDF for organization {}", inventoryId, organizationId);
    StockInventory inventory = stockInventoryRepository.findById(inventoryId)
        .orElseThrow(() -> new ResourceNotFoundException("Stock inventory count sheet not found with id: " + inventoryId));

    if (!inventory.getOrganization().getId().equals(organizationId)) {
      throw new IllegalArgumentException("Stock inventory count sheet does not belong to organization: " + organizationId);
    }

    Document document = new Document();
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    try {
      PdfWriter.getInstance(document, out);
      document.open();

      // Title
      Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
      Paragraph title = new Paragraph("INVENTORY COUNT AUDIT REPORT", titleFont);
      title.setAlignment(Element.ALIGN_CENTER);
      document.add(title);
      document.add(new Paragraph(" "));

      // Metadata
      Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
      document.add(new Paragraph("Inventory Title: " + inventory.getName(), metaFont));
      document.add(new Paragraph("State: " + inventory.getState(), metaFont));
      if (inventory.getLocation() != null) {
        document.add(new Paragraph("Location Scope: " + inventory.getLocation().getCode(), metaFont));
      } else {
        document.add(new Paragraph("Location Scope: All Locations", metaFont));
      }
      if (inventory.getInventoryDate() != null) {
        document.add(new Paragraph("Inventory Date: " + DATE_FORMATTER.format(inventory.getInventoryDate()), metaFont));
      }
      document.add(new Paragraph(" "));

      // Table of Lines
      PdfPTable table = new PdfPTable(6);
      table.setWidthPercentage(100);
      table.setWidths(new float[]{2f, 3f, 2f, 1.5f, 1.5f, 1.5f});

      // Headers
      String[] headers = {"Product SKU", "Product Name", "Location", "System Qty", "Checked Qty", "Variance"};
      for (String header : headers) {
        PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9)));
        cell.setBackgroundColor(Color.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
      }

      // Data Rows
      for (StockInventoryLine line : inventory.getInventoryLines()) {
        table.addCell(new Phrase(line.getProduct().getSku(), metaFont));
        table.addCell(new Phrase(line.getProduct().getName(), metaFont));
        table.addCell(new Phrase(line.getLocation().getCode(), metaFont));
        table.addCell(new Phrase(line.getTheoreticalQty().toString(), metaFont));
        table.addCell(new Phrase(line.getCheckedQty().toString(), metaFont));
        
        BigDecimal variance = line.getCheckedQty().subtract(line.getTheoreticalQty());
        Phrase varPhrase = new Phrase(variance.toString(), metaFont);
        if (variance.compareTo(BigDecimal.ZERO) < 0) {
          varPhrase = new Phrase(variance.toString(), FontFactory.getFont(FontFactory.HELVETICA, 10, Color.RED));
        } else if (variance.compareTo(BigDecimal.ZERO) > 0) {
          varPhrase = new Phrase("+" + variance, FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(0, 128, 0)));
        }
        table.addCell(varPhrase);
      }

      document.add(table);
      document.close();

    } catch (DocumentException e) {
      log.error("Failed to generate PDF for inventory audit", e);
      throw new RuntimeException("PDF generation failed", e);
    }

    return new ByteArrayInputStream(out.toByteArray());
  }
}
