package com.dut.erp.service.impl;

import com.dut.erp.dto.response.StockValuationResponse;
import com.dut.erp.entity.Order;
import com.dut.erp.entity.StockValuation;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.repository.OrderRepository;
import com.dut.erp.repository.StockValuationRepository;
import com.dut.erp.service.StockValuationService;
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
public class StockValuationServiceImpl implements StockValuationService {

  private final StockValuationRepository stockValuationRepository;
  private final OrderRepository orderRepository;

  @Override
  public List<StockValuationResponse> getValuationsByOrderId(UUID organizationId, UUID orderId) {
    log.info("Fetching stock valuations for order {} in org {}", orderId, organizationId);
    
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    
    if (!order.getOrganization().getId().equals(organizationId)) {
      throw new BadRequestException("Order does not belong to the requested organization");
    }

    List<StockValuation> valuations = stockValuationRepository.findAllByOrderId(orderId);

    return valuations.stream()
        .map(sv -> new StockValuationResponse(
            sv.getId(),
            sv.getInventoryDocumentLine().getId(),
            sv.getProduct().getId(),
            sv.getProduct().getName(),
            sv.getQuantity(),
            sv.getUnitCost(),
            sv.getTotalValuation(),
            sv.getProduct().getSalesPrice(),
            sv.getMethod(),
            sv.getCreatedAt()
        ))
        .collect(Collectors.toList());
  }
}
