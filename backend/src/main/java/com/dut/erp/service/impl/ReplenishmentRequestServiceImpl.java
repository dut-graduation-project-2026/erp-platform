package com.dut.erp.service.impl;

import com.dut.erp.constant.SortingConstants;
import com.dut.erp.dto.common.SortField;
import com.dut.erp.dto.request.CreateReplenishmentRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.ReplenishmentRequestResponse;
import com.dut.erp.dto.response.UserBaseResponse;
import com.dut.erp.entity.InventoryDocument;
import com.dut.erp.entity.ReplenishmentRequest;
import com.dut.erp.entity.Warehouse;
import com.dut.erp.enums.DocumentStatus;
import com.dut.erp.enums.ReplenishmentStatus;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.repository.InventoryDocumentRepository;
import com.dut.erp.repository.ReplenishmentRequestRepository;
import com.dut.erp.repository.WarehouseRepository;
import com.dut.erp.repository.OrderRepository;
import com.dut.erp.entity.Order;
import com.dut.erp.service.ReplenishmentRequestService;
import com.dut.erp.dto.event.ReplenishmentRequestStatusChangedEvent;
import org.springframework.context.ApplicationEventPublisher;
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
public class ReplenishmentRequestServiceImpl implements ReplenishmentRequestService {

  private final WarehouseRepository warehouseRepository;
  private final InventoryDocumentRepository inventoryDocumentRepository;
  private final ReplenishmentRequestRepository replenishmentRequestRepository;
  private final OrderRepository orderRepository;
  private final ApplicationEventPublisher applicationEventPublisher;

  @Override
  @Transactional
  public ReplenishmentRequestResponse createReplenishmentRequest(
      UUID organizationId, UUID warehouseId, CreateReplenishmentRequest request) {
    log.info("Creating replenishment request for warehouse {}", warehouseId);
    Warehouse warehouse = warehouseRepository.findByIdAndOrganizationId(warehouseId, organizationId)
        .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found: " + warehouseId));

    InventoryDocument doc = inventoryDocumentRepository.findByIdAndWarehouseId(request.inventoryDocumentId(), warehouseId)
        .orElseThrow(() -> new ResourceNotFoundException("Inventory document not found: " + request.inventoryDocumentId()));

    if (doc.getDocumentStatus() != DocumentStatus.WAITING_FOR_STOCK) {
      throw new BadRequestException("Replenishment request can only be created for documents waiting for stock");
    }

    // Check if one already exists for this document to avoid duplicates
    if (replenishmentRequestRepository.findByInventoryDocumentId(doc.getId()).isPresent()) {
      throw new BadRequestException("Replenishment request already exists for this document");
    }

    ReplenishmentRequest req = ReplenishmentRequest.builder()
        .warehouse(warehouse)
        .inventoryDocument(doc)
        .notes(request.notes())
        .status(ReplenishmentStatus.OPEN)
        .build();

    req = replenishmentRequestRepository.save(req);
    applicationEventPublisher.publishEvent(new ReplenishmentRequestStatusChangedEvent(req.getId(), null, ReplenishmentStatus.OPEN));
    return mapToResponse(req);
  }

  @Override
  public PagedEntityResponse<ReplenishmentRequestResponse> getReplenishmentRequests(
      UUID organizationId, UUID warehouseId, String search, String status, PaginationRequest paginationRequest) {
    log.info("Fetching replenishment requests for warehouse {}", warehouseId);

    Pageable pageable = PageRequest.of(
        paginationRequest.page() - 1,
        paginationRequest.limit(),
        SortingConstants.customEntitiesSort(SortField.desc("createdAt"))
    );

    com.dut.erp.enums.ReplenishmentStatus repStatus = null;
    if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
      try {
        repStatus = com.dut.erp.enums.ReplenishmentStatus.valueOf(status.trim().toUpperCase());
      } catch (IllegalArgumentException e) {
        log.warn("Invalid status value: {}", status);
      }
    }

    Page<UUID> ids = (search != null && !search.trim().isEmpty())
        ? replenishmentRequestRepository.findIdsByWarehouseIdAndSearch(warehouseId, search, repStatus, pageable)
        : replenishmentRequestRepository.findIdsByWarehouseId(warehouseId, repStatus, pageable);

    if (ids.isEmpty()) {
      return PagedEntityResponse.from(Page.empty(pageable));
    }

    List<ReplenishmentRequest> requests = replenishmentRequestRepository.findAllByIdIn(ids.getContent());
    Map<UUID, ReplenishmentRequest> reqMap = requests.stream()
        .collect(Collectors.toMap(ReplenishmentRequest::getId, Function.identity()));

    List<ReplenishmentRequestResponse> responses = ids.getContent().stream()
        .map(reqMap::get)
        .filter(Objects::nonNull)
        .map(this::mapToResponse)
        .collect(Collectors.toList());

    return PagedEntityResponse.from(new PageImpl<>(responses, pageable, ids.getTotalElements()));
  }

  private ReplenishmentRequestResponse mapToResponse(ReplenishmentRequest req) {
    UserBaseResponse createdByResp = req.getCreatedBy() != null
        ? new UserBaseResponse(req.getCreatedBy().getId(), req.getCreatedBy().getEmail(), req.getCreatedBy().getFirstName(), req.getCreatedBy().getLastName())
        : null;

    String orderNumber = null;
    if (req.getInventoryDocument() != null 
        && req.getInventoryDocument().getReferenceType() == com.dut.erp.enums.ReferenceType.SALES_ORDER 
        && req.getInventoryDocument().getReferenceId() != null) {
        Order order = orderRepository.findById(req.getInventoryDocument().getReferenceId()).orElse(null);
        if (order != null) {
            orderNumber = order.getOrderNumber();
        }
    }

    UUID referenceId = req.getInventoryDocument() != null ? req.getInventoryDocument().getReferenceId() : null;

    return new ReplenishmentRequestResponse(
        req.getId(),
        req.getWarehouse().getId(),
        req.getWarehouse().getName(),
        req.getInventoryDocument().getId(),
        req.getInventoryDocument().getName(),
        req.getNotes(),
        req.getStatus().name(),
        req.getCreatedAt(),
        createdByResp,
        orderNumber,
        referenceId
    );
  }
}
