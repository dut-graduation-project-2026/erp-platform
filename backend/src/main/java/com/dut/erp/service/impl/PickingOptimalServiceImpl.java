package com.dut.erp.service.impl;

import com.dut.erp.dto.response.StockQuantResponse;
import com.dut.erp.entity.StockQuant;
import com.dut.erp.mapper.StockQuantMapper;
import com.dut.erp.repository.StockQuantRepository;
import com.dut.erp.service.PickingOptimalService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PickingOptimalServiceImpl implements PickingOptimalService {

  private final StockQuantRepository stockQuantRepository;
  private final StockQuantMapper stockQuantMapper;

  @Override
  public List<StockQuantResponse> suggestLocations(UUID organizationId, UUID productId, BigDecimal quantity) {
    log.info("Suggesting optimal locations for product {} and quantity {}", productId, quantity);

    List<StockQuant> quants = stockQuantRepository.findAvailableQuantsByProduct(productId).stream()
        .filter(q -> q.getQuantity().subtract(q.getReservedQuantity()).compareTo(BigDecimal.ZERO) > 0)
        .sorted((q1, q2) -> {
          BigDecimal avail1 = q1.getQuantity().subtract(q1.getReservedQuantity());
          BigDecimal avail2 = q2.getQuantity().subtract(q2.getReservedQuantity());
          return avail2.compareTo(avail1);
        })
        .collect(Collectors.toList());

    List<StockQuantResponse> suggestions = new ArrayList<>();
    BigDecimal remaining = quantity;

    for (StockQuant quant : quants) {
      BigDecimal available = quant.getQuantity().subtract(quant.getReservedQuantity());
      BigDecimal suggestQty = available.min(remaining);

      StockQuant suggestionQuant = StockQuant.builder()
          .id(quant.getId())
          .product(quant.getProduct())
          .location(quant.getLocation())
          .lot(quant.getLot())
          .quantity(suggestQty)
          .reservedQuantity(BigDecimal.ZERO)
          .build();

      suggestions.add(stockQuantMapper.toResponse(suggestionQuant));
      remaining = remaining.subtract(suggestQty);
      if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
        break;
      }
    }

    return suggestions;
  }
}
