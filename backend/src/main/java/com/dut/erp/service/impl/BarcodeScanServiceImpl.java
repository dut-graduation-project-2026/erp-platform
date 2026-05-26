package com.dut.erp.service.impl;

import com.dut.erp.dto.response.BarcodeScanResponse;
import com.dut.erp.dto.response.ProductResponse;
import com.dut.erp.dto.response.StockLocationResponse;
import com.dut.erp.dto.response.StockLotResponse;
import com.dut.erp.entity.Organization;
import com.dut.erp.entity.Product;
import com.dut.erp.entity.StockLocation;
import com.dut.erp.entity.StockLot;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.ProductMapper;
import com.dut.erp.mapper.StockLocationMapper;
import com.dut.erp.mapper.StockLotMapper;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.repository.ProductRepository;
import com.dut.erp.repository.StockLocationRepository;
import com.dut.erp.repository.StockLotRepository;
import com.dut.erp.service.BarcodeScanService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BarcodeScanServiceImpl implements BarcodeScanService {

  private final OrganizationRepository organizationRepository;
  private final ProductRepository productRepository;
  private final StockLocationRepository stockLocationRepository;
  private final StockLotRepository stockLotRepository;
  private final ProductMapper productMapper;
  private final StockLocationMapper stockLocationMapper;
  private final StockLotMapper stockLotMapper;

  @Override
  public BarcodeScanResponse scanBarcode(UUID organizationId, String barcode) {
    log.info("Scanning barcode '{}' in organization {}", barcode, organizationId);
    findOrganizationById(organizationId);

    // 1. Try to find product by barcode
    Optional<Product> productOpt = productRepository.findByBarcodeAndOrganizationId(barcode, organizationId);
    if (productOpt.isPresent()) {
      Product product = productOpt.get();
      ProductResponse response = productMapper.toResponse(product);
      return new BarcodeScanResponse("PRODUCT", product.getId(), product.getBarcode(), response);
    }

    // 1b. Try to find product by SKU (often used as fallback barcode)
    Optional<Product> productSkuOpt = productRepository.findBySkuAndOrganizationId(barcode, organizationId);
    if (productSkuOpt.isPresent()) {
      Product product = productSkuOpt.get();
      ProductResponse response = productMapper.toResponse(product);
      return new BarcodeScanResponse("PRODUCT", product.getId(), product.getSku(), response);
    }

    // 2. Try to find location by code
    List<StockLocation> locations = stockLocationRepository.findAllByCodeAndOrganizationId(barcode, organizationId);
    if (!locations.isEmpty()) {
      StockLocation location = locations.get(0);
      StockLocationResponse response = stockLocationMapper.toResponse(location);
      return new BarcodeScanResponse("LOCATION", location.getId(), location.getCode(), response);
    }

    // 3. Try to find lot by lot number
    List<StockLot> lots = stockLotRepository.findAllByLotNumberAndOrganizationId(barcode, organizationId);
    if (!lots.isEmpty()) {
      StockLot lot = lots.get(0);
      StockLotResponse response = stockLotMapper.toResponse(lot);
      return new BarcodeScanResponse("LOT", lot.getId(), lot.getLotNumber(), response);
    }

    throw new ResourceNotFoundException("Scanned barcode '" + barcode + "' was not recognized.");
  }

  private Organization findOrganizationById(UUID organizationId) {
    return organizationRepository
        .findById(organizationId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "Organization not found with id: " + organizationId));
  }
}
