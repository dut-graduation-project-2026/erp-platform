package com.dut.erp.service.impl;

import com.dut.erp.dto.request.CreateStockLotRequest;
import com.dut.erp.dto.response.StockLotResponse;
import com.dut.erp.entity.Organization;
import com.dut.erp.entity.Product;
import com.dut.erp.entity.StockLot;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.exception.ResourceAlreadyExistsException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.StockLotMapper;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.repository.ProductRepository;
import com.dut.erp.repository.StockLotRepository;
import com.dut.erp.service.StockLotService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockLotServiceImpl implements StockLotService {

  private final OrganizationRepository organizationRepository;
  private final ProductRepository productRepository;
  private final StockLotRepository stockLotRepository;
  private final StockLotMapper stockLotMapper;

  @Override
  @Transactional
  public StockLotResponse createLot(UUID organizationId, CreateStockLotRequest request) {
    log.info("Creating stock lot {} for product {} in org {}", request.lotNumber(), request.productId(), organizationId);
    Organization organization = findOrganizationById(organizationId);
    
    Product product = productRepository.findById(request.productId())
        .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.productId()));
    if (!product.getOrganization().getId().equals(organizationId)) {
      throw new BadRequestException("Product does not belong to the specified organization.");
    }

    if (stockLotRepository.findByLotNumberAndProductIdAndOrganizationId(request.lotNumber(), request.productId(), organizationId).isPresent()) {
      throw new ResourceAlreadyExistsException("Lot number '" + request.lotNumber() + "' already exists for this product.");
    }

    StockLot lot = StockLot.builder()
        .organization(organization)
        .product(product)
        .lotNumber(request.lotNumber())
        .expirationDate(request.expirationDate())
        .build();

    lot = stockLotRepository.save(lot);
    return stockLotMapper.toResponse(lot);
  }

  @Override
  public StockLotResponse getLotById(UUID organizationId, UUID lotId) {
    StockLot lot = stockLotRepository.findByIdAndOrganizationId(lotId, organizationId)
        .orElseThrow(() -> new ResourceNotFoundException("Lot not found: " + lotId));
    return stockLotMapper.toResponse(lot);
  }

  @Override
  public List<StockLotResponse> getLotsByProductId(UUID organizationId, UUID productId) {
    return stockLotRepository.findAllByProductIdAndOrganizationId(productId, organizationId).stream()
        .map(stockLotMapper::toResponse)
        .collect(Collectors.toList());
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
