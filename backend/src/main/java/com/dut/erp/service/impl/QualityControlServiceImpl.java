package com.dut.erp.service.impl;

import com.dut.erp.dto.request.QcInspectionRequest;
import com.dut.erp.dto.response.StockPickingResponse;
import com.dut.erp.entity.StockMove;
import com.dut.erp.entity.StockPicking;
import com.dut.erp.enums.StockPickingState;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.StockPickingMapper;
import com.dut.erp.repository.StockMoveRepository;
import com.dut.erp.repository.StockPickingRepository;
import com.dut.erp.service.QualityControlService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class QualityControlServiceImpl implements QualityControlService {

  private final StockPickingRepository stockPickingRepository;
  private final StockMoveRepository stockMoveRepository;
  private final StockPickingMapper stockPickingMapper;

  @Override
  public StockPickingResponse inspectPicking(UUID organizationId, UUID pickingId, QcInspectionRequest request) {
    log.info("Performing QC inspection for picking {}", pickingId);
    StockPicking picking = stockPickingRepository.findByIdAndOrganizationId(pickingId, organizationId)
        .orElseThrow(() -> new ResourceNotFoundException("Stock picking not found: " + pickingId));

    if (picking.getState() == StockPickingState.DONE || picking.getState() == StockPickingState.CANCEL) {
      throw new BadRequestException("Cannot perform QC on a finalized picking.");
    }

    for (QcInspectionRequest.QcLine qcLine : request.qcLines()) {
      StockMove move = picking.getStockMoves().stream()
          .filter(m -> m.getId().equals(qcLine.moveId()))
          .findFirst()
          .orElseThrow(() -> new ResourceNotFoundException("Stock move line not found: " + qcLine.moveId()));

      if (qcLine.approvedQty().compareTo(move.getProductUomQty()) > 0) {
        throw new BadRequestException("Approved quantity cannot exceed initial quantity: " 
            + move.getProductUomQty());
      }

      move.setQuantityDone(qcLine.approvedQty());
      stockMoveRepository.save(move);
      log.info("QC line {} approved quantity set to {}", move.getId(), qcLine.approvedQty());
    }

    picking = stockPickingRepository.save(picking);
    return stockPickingMapper.toResponse(picking);
  }
}
