package com.dut.erp.service.impl;

import com.dut.erp.constant.SortingConstants;
import com.dut.erp.dto.common.SortField;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.StockQuantResponse;
import com.dut.erp.entity.Product;
import com.dut.erp.enums.LocationType;
import com.dut.erp.entity.StockLocation;
import com.dut.erp.entity.StockLot;
import com.dut.erp.entity.StockQuant;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.StockQuantMapper;
import com.dut.erp.repository.ProductRepository;
import com.dut.erp.repository.StockLocationRepository;
import com.dut.erp.repository.StockLotRepository;
import com.dut.erp.repository.StockQuantRepository;
import com.dut.erp.service.StockQuantService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockQuantServiceImpl implements StockQuantService {

  private final ProductRepository productRepository;
  private final StockLocationRepository stockLocationRepository;
  private final StockLotRepository stockLotRepository;
  private final StockQuantRepository stockQuantRepository;
  private final StockQuantMapper stockQuantMapper;

  @Override
  public PagedEntityResponse<StockQuantResponse> getStockQuants(
      UUID organizationId,
      UUID productId,
      UUID warehouseId,
      UUID locationId,
      PaginationRequest paginationRequest
  ) {
    log.info("Fetching stock quants with filters product={}, warehouse={}, location={}", 
        productId, warehouseId, locationId);

    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit(),
            SortingConstants.customEntitiesSort(SortField.desc("quantity")));

    Page<UUID> ids = stockQuantRepository.findIdsByFilters(
        organizationId, productId, warehouseId, locationId, pageable);

    if (ids.isEmpty()) {
      return PagedEntityResponse.from(Page.empty(pageable));
    }

    Map<UUID, StockQuant> quantMap =
        stockQuantRepository.findAllByIdIn(ids.getContent()).stream()
            .collect(Collectors.toMap(StockQuant::getId, Function.identity()));

    List<StockQuantResponse> responses =
        ids.getContent().stream()
            .map(quantMap::get)
            .filter(Objects::nonNull)
            .map(stockQuantMapper::toResponse)
            .collect(Collectors.toList());

    return PagedEntityResponse.from(new PageImpl<>(responses, pageable, ids.getTotalElements()));
  }

  @Override
  @Transactional
  public void addStock(UUID organizationId, UUID productId, UUID locationId, UUID lotId, BigDecimal quantity) {
    log.info("Adding {} stock of product {} to location {}", quantity, productId, locationId);
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
    StockLocation location = stockLocationRepository.findById(locationId)
        .orElseThrow(() -> new ResourceNotFoundException("Location not found: " + locationId));
    
    final StockLot finalLot;
    if (lotId != null) {
      finalLot = stockLotRepository.findById(lotId)
          .orElseThrow(() -> new ResourceNotFoundException("Lot not found: " + lotId));
    } else {
      finalLot = null;
    }

    StockQuant quant = stockQuantRepository.findByProductIdLocationIdAndLotId(productId, locationId, lotId)
        .orElseGet(() -> StockQuant.builder()
            .product(product)
            .location(location)
            .lot(finalLot)
            .quantity(BigDecimal.ZERO)
            .reservedQuantity(BigDecimal.ZERO)
            .onOrderQuantity(BigDecimal.ZERO)
            .build());

    quant.setQuantity(quant.getQuantity().add(quantity));
    
    if (location.getLocationType() == LocationType.INTERNAL && quant.getQuantity().compareTo(BigDecimal.ZERO) < 0) {
      throw new BadRequestException("Stock quantity cannot be negative for internal location: " + location.getCode());
    }

    stockQuantRepository.save(quant);
  }

  @Override
  @Transactional
  public void reserveStock(UUID organizationId, UUID productId, UUID locationId, UUID lotId, BigDecimal quantity) {
    log.info("Reserving {} stock of product {} in location {}", quantity, productId, locationId);
    StockQuant quant = stockQuantRepository.findByProductIdLocationIdAndLotId(productId, locationId, lotId)
        .orElseThrow(() -> new BadRequestException("No stock exists in this location to reserve."));

    BigDecimal available = quant.getQuantity().subtract(quant.getReservedQuantity());
    if (available.compareTo(quantity) < 0) {
      throw new BadRequestException("Insufficient available stock to reserve. Available: " 
          + available + ", requested: " + quantity);
    }

    quant.setReservedQuantity(quant.getReservedQuantity().add(quantity));
    stockQuantRepository.save(quant);
  }

  @Override
  @Transactional
  public void releaseReservedStock(UUID organizationId, UUID productId, UUID locationId, UUID lotId, BigDecimal quantity) {
    log.info("Releasing {} reserved stock of product {} in location {}", quantity, productId, locationId);
    StockQuant quant = stockQuantRepository.findByProductIdLocationIdAndLotId(productId, locationId, lotId)
        .orElseThrow(() -> new ResourceNotFoundException("Stock quant record not found."));

    if (quant.getReservedQuantity().compareTo(quantity) < 0) {
      throw new BadRequestException("Cannot release more than reserved stock. Reserved: " 
          + quant.getReservedQuantity() + ", requested release: " + quantity);
    }

    quant.setReservedQuantity(quant.getReservedQuantity().subtract(quantity));
    stockQuantRepository.save(quant);
  }
}
