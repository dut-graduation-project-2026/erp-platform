package com.dut.erp.service.impl;

import com.dut.erp.constant.SortingConstants;
import com.dut.erp.dto.common.SortField;
import com.dut.erp.dto.request.CreateInventoryDocumentRequest;
import com.dut.erp.dto.request.InventoryDocumentItemRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.response.InventoryDocumentBaseResponse;
import com.dut.erp.dto.response.InventoryDocumentResponse;
import com.dut.erp.dto.response.InventoryDocumentLineResponse;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.UserBaseResponse;
import com.dut.erp.entity.InventoryBalance;
import com.dut.erp.entity.InventoryDocument;
import com.dut.erp.entity.InventoryDocumentLine;
import com.dut.erp.entity.Order;
import com.dut.erp.entity.OrderItem;
import com.dut.erp.entity.Product;
import com.dut.erp.entity.ReplenishmentRequest;
import com.dut.erp.entity.Warehouse;
import com.dut.erp.enums.DocumentStatus;
import com.dut.erp.enums.DocumentType;
import com.dut.erp.enums.OrderStatus;
import com.dut.erp.enums.ReferenceType;
import com.dut.erp.enums.ReplenishmentStatus;
import com.dut.erp.enums.LeadStage;
import com.dut.erp.dto.event.OrderStatusChangedEvent;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.repository.InventoryBalanceRepository;
import com.dut.erp.repository.InventoryDocumentRepository;
import com.dut.erp.repository.InventoryDocumentLineRepository;
import com.dut.erp.repository.OrderRepository;
import com.dut.erp.repository.ProductRepository;
import com.dut.erp.repository.ReplenishmentRequestRepository;
import com.dut.erp.entity.Invoice;
import com.dut.erp.enums.InvoiceStatus;
import com.dut.erp.repository.InvoiceRepository;
import com.dut.erp.repository.WarehouseRepository;
import com.dut.erp.service.InventoryDocumentService;
import com.dut.erp.service.COGSValuationEngine;
import com.dut.erp.dto.event.ReplenishmentRequestStatusChangedEvent;
import org.springframework.context.ApplicationEventPublisher;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
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
import com.dut.erp.service.SecurityAuthService;
import com.dut.erp.util.SecurityUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryDocumentServiceImpl implements InventoryDocumentService {

  private final WarehouseRepository warehouseRepository;
  private final ProductRepository productRepository;
  private final InventoryDocumentRepository inventoryDocumentRepository;
  private final InventoryDocumentLineRepository inventoryDocumentLineRepository;
  private final InventoryBalanceRepository inventoryBalanceRepository;
  private final OrderRepository orderRepository;
  private final ReplenishmentRequestRepository replenishmentRequestRepository;
  private final InvoiceRepository invoiceRepository;
  private final COGSValuationEngine cogsValuationEngine;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final SecurityAuthService securityAuthService;

  @Override
  @Transactional
  public InventoryDocumentResponse createDocument(
      UUID organizationId, UUID warehouseId, CreateInventoryDocumentRequest request) {
    log.info("Creating manual inventory document of type {} in warehouse {}", request.documentType(), warehouseId);

    // Validate quantities based on document type
    for (var item : request.items()) {
      if (request.documentType() == DocumentType.ADJUSTMENT) {
        if (item.quantity().compareTo(BigDecimal.ZERO) == 0) {
          throw new BadRequestException("Adjustment quantity cannot be zero.");
        }
      } else {
        if (item.quantity().compareTo(BigDecimal.ZERO) <= 0) {
          throw new BadRequestException("Quantity must be positive for " + request.documentType() + ".");
        }
      }
    }

    Warehouse warehouse = findWarehouseByIdAndOrganizationId(warehouseId, organizationId);

    // Batch fetch all products in the request to optimize DB calls
    List<UUID> productIds = request.items().stream()
        .map(InventoryDocumentItemRequest::productId)
        .distinct()
        .collect(Collectors.toList());
    List<Product> products = productRepository.findAllByIdInAndOrganizationId(productIds, organizationId);
    if (products.size() < productIds.size()) {
      List<UUID> foundIds = products.stream().map(Product::getId).toList();
      List<UUID> missingIds = productIds.stream().filter(id -> !foundIds.contains(id)).toList();
      throw new ResourceNotFoundException("The requested product(s) could not be found or do not belong to this organization: " + missingIds);
    }
    Map<UUID, Product> productMap = products.stream()
        .collect(Collectors.toMap(Product::getId, Function.identity()));

    Warehouse sourceWarehouse = null;
    if (request.documentType() == DocumentType.TRANSFER_IN || request.documentType() == DocumentType.TRANSFER_OUT) {
      if (request.transferSourceWarehouseId() == null) {
        throw new BadRequestException("Both source and destination warehouses are required for a transfer.");
      }
      if (request.transferSourceWarehouseId().equals(warehouseId)) {
        throw new BadRequestException("Source and destination warehouses must be different.");
      }
      sourceWarehouse = findWarehouseByIdAndOrganizationId(request.transferSourceWarehouseId(), organizationId);

      Warehouse sourceWh;
      Warehouse destWh;
      if (request.documentType() == DocumentType.TRANSFER_IN) {
        destWh = warehouse;
        sourceWh = sourceWarehouse;
      } else {
        sourceWh = warehouse;
        destWh = sourceWarehouse;
      }

      String baseNotes = request.notes() != null ? request.notes() : "";
      String currentUserName = SecurityUtils.getCurrentUser().getEmail();
      String docInNotes = baseNotes;
      String docOutNotes = baseNotes;
      
      if (request.documentType() == DocumentType.TRANSFER_IN) {
        docOutNotes = baseNotes + (baseNotes.isEmpty() ? "" : "\n") + "[System] Transfer requested by " + currentUserName + " from " + destWh.getName();
      } else {
        docInNotes = baseNotes + (baseNotes.isEmpty() ? "" : "\n") + "[System] Transfer initiated by " + currentUserName + " from " + sourceWh.getName();
      }

      // Create docIn (Inbound Transfer at destination warehouse)
      InventoryDocument docIn = InventoryDocument.builder()
          .warehouse(destWh)
          .sourceWarehouse(sourceWh)
          .name(generateDocumentName(DocumentType.TRANSFER_IN))
          .documentType(DocumentType.TRANSFER_IN)
          .referenceType(request.replenishmentRequestId() != null ? ReferenceType.REPLENISHMENT : ReferenceType.MANUAL)
          .referenceId(request.replenishmentRequestId() != null ? request.replenishmentRequestId() : null)
          .documentStatus(DocumentStatus.DRAFT)
          .scheduledDate(request.scheduledDate())
          .notes(docInNotes)
          .build();

      List<InventoryDocumentLine> linesIn = new ArrayList<>();
      for (var item : request.items()) {
        Product product = productMap.get(item.productId());
        linesIn.add(InventoryDocumentLine.builder()
            .inventoryDocument(docIn)
            .product(product)
            .quantity(item.quantity())
            .unitCost(product.getPurchasePrice())
            .valuation(item.quantity().multiply(product.getPurchasePrice()))
            .build());
      }
      docIn.setLines(linesIn);
      InventoryDocument savedDocIn = inventoryDocumentRepository.save(docIn);

      // Create docOut (Outbound Transfer at source warehouse)
      InventoryDocument docOut = InventoryDocument.builder()
          .warehouse(sourceWh)
          .sourceWarehouse(destWh)
          .name(generateDocumentName(DocumentType.TRANSFER_OUT))
          .documentType(DocumentType.TRANSFER_OUT)
          .referenceType(ReferenceType.MANUAL)
          .referenceId(savedDocIn.getId()) // Link to docIn
          .documentStatus(DocumentStatus.DRAFT)
          .scheduledDate(request.scheduledDate())
          .notes(docOutNotes)
          .build();

      List<InventoryDocumentLine> linesOut = new ArrayList<>();
      for (var item : request.items()) {
        Product product = productMap.get(item.productId());
        linesOut.add(InventoryDocumentLine.builder()
            .inventoryDocument(docOut)
            .product(product)
            .quantity(item.quantity())
            .unitCost(product.getPurchasePrice())
            .valuation(item.quantity().multiply(product.getPurchasePrice()))
            .build());
      }
      docOut.setLines(linesOut);
      InventoryDocument savedDocOut = inventoryDocumentRepository.save(docOut);

      // Link docIn back to docOut if not replenishment
      if (request.replenishmentRequestId() == null) {
        savedDocIn.setReferenceId(savedDocOut.getId());
        savedDocIn = inventoryDocumentRepository.save(savedDocIn);
      }

      if (request.documentType() == DocumentType.TRANSFER_IN) {
        return mapToResponse(savedDocIn);
      } else {
        return mapToResponse(savedDocOut);
      }
    }

    ReferenceType refType = ReferenceType.MANUAL;
    UUID refId = null;
    if (request.replenishmentRequestId() != null) {
      refType = ReferenceType.REPLENISHMENT;
      refId = request.replenishmentRequestId();
    }

    InventoryDocument doc = InventoryDocument.builder()
        .warehouse(warehouse)
        .sourceWarehouse(sourceWarehouse)
        .name(generateDocumentName(request.documentType()))
        .documentType(request.documentType())
        .referenceType(refType)
        .referenceId(refId)
        .documentStatus(DocumentStatus.DRAFT)
        .scheduledDate(request.scheduledDate())
        .notes(request.notes())
        .build();

    List<InventoryDocumentLine> lines = request.items().stream()
        .map(item -> {
          Product product = productMap.get(item.productId());
          return InventoryDocumentLine.builder()
              .inventoryDocument(doc)
              .product(product)
              .quantity(item.quantity())
              .unitCost(product.getPurchasePrice())
              .valuation(item.quantity().multiply(product.getPurchasePrice()))
              .build();
        })
        .collect(Collectors.toList());

    doc.setLines(lines);
    InventoryDocument savedDoc = inventoryDocumentRepository.save(doc);

    return mapToResponse(savedDoc);
  }

  @Override
  @Transactional
  public InventoryDocumentResponse createIssueDocumentFromOrder(UUID organizationId, UUID warehouseId, UUID orderId) {
    log.info("Creating issue document from sales order {} for warehouse {}", orderId, warehouseId);
    Warehouse warehouse = findWarehouseByIdAndOrganizationId(warehouseId, organizationId);

    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("The requested order could not be found."));
    if (!order.getOrganization().getId().equals(organizationId)) {
      throw new BadRequestException("This order does not belong to your organization.");
    }

    if (order.getStatus() != OrderStatus.CONFIRMED) {
      throw new BadRequestException("Only confirmed orders can be processed for inventory.");
    }

    // Check duplicate claim
    boolean alreadyClaimed = inventoryDocumentRepository
        .existsByReferenceTypeAndReferenceIdAndDocumentTypeAndDocumentStatusNot(
            ReferenceType.SALES_ORDER, orderId, DocumentType.ISSUE, DocumentStatus.CANCELLED);
    if (alreadyClaimed) {
      throw new BadRequestException("This order has already been processed for inventory.");
    }

    InventoryDocument doc = InventoryDocument.builder()
        .warehouse(warehouse)
        .name(generateDocumentName(DocumentType.ISSUE))
        .documentType(DocumentType.ISSUE)
        .referenceType(ReferenceType.SALES_ORDER)
        .referenceId(orderId)
        .documentStatus(DocumentStatus.DRAFT)
        .scheduledDate(Instant.now())
        .build();

    List<InventoryDocumentLine> lines = new ArrayList<>();
    for (OrderItem item : order.getItems()) {
      BigDecimal productPrice = item.getProduct().getPurchasePrice();
      lines.add(InventoryDocumentLine.builder()
          .inventoryDocument(doc)
          .product(item.getProduct())
          .quantity(item.getQuantity())
          .unitCost(productPrice)
          .valuation(item.getQuantity().multiply(productPrice))
          .build());
    }
    doc.setLines(lines);

    // Stock check
    List<UUID> productIds = lines.stream()
        .map(tx -> tx.getProduct().getId())
        .collect(Collectors.toList());
    List<InventoryBalance> balances = inventoryBalanceRepository
        .findAllByWarehouseIdAndProductIdIn(warehouseId, productIds);
    Map<UUID, InventoryBalance> balanceMap = balances.stream()
        .collect(Collectors.toMap(ib -> ib.getProduct().getId(), Function.identity()));

    boolean isSufficient = true;
    for (InventoryDocumentLine tx : lines) {
      InventoryBalance balance = balanceMap.get(tx.getProduct().getId());
      if (balance == null) {
        throw new ResourceNotFoundException("Inventory balance not found for product: " + tx.getProduct().getName() + ".");
      }
      if (balance.getQuantity().compareTo(tx.getQuantity()) < 0) {
        isSufficient = false;
      }
    }

    if (isSufficient) {
      doc.setDocumentStatus(DocumentStatus.CONFIRMED);
      order.setStatus(OrderStatus.SENT);
    } else {
      doc.setDocumentStatus(DocumentStatus.WAITING_FOR_STOCK);
      order.setStatus(OrderStatus.WAITING_FOR_STOCK);
    }

    orderRepository.save(order);
    doc = inventoryDocumentRepository.save(doc);

    if (doc.getDocumentStatus() == DocumentStatus.WAITING_FOR_STOCK) {
      ReplenishmentRequest replenishmentRequest = ReplenishmentRequest.builder()
          .warehouse(warehouse)
          .inventoryDocument(doc)
          .notes("Auto-generated replenishment for order claim.")
          .status(ReplenishmentStatus.OPEN)
          .build();
      replenishmentRequestRepository.save(replenishmentRequest);
      applicationEventPublisher.publishEvent(new ReplenishmentRequestStatusChangedEvent(replenishmentRequest.getId(), null, ReplenishmentStatus.OPEN));
    }

    return mapToResponse(doc);
  }

  @Override
  public PagedEntityResponse<InventoryDocumentBaseResponse> getDocuments(
      UUID organizationId, UUID warehouseId, String search, String status, String type, PaginationRequest paginationRequest) {
    log.info("Fetching documents for warehouse {}", warehouseId);
    findWarehouseByIdAndOrganizationId(warehouseId, organizationId);

    Pageable pageable = PageRequest.of(
        paginationRequest.page() - 1,
        paginationRequest.limit()
    );

    com.dut.erp.enums.DocumentStatus docStatus = null;
    if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
      try {
        docStatus = com.dut.erp.enums.DocumentStatus.valueOf(status.trim().toUpperCase());
      } catch (IllegalArgumentException e) {
        log.warn("Invalid status value: {}", status);
      }
    }

    java.util.List<com.dut.erp.enums.DocumentType> docTypes = new java.util.ArrayList<>();
    boolean useTypeFilter = false;
    if (type != null && !type.trim().isEmpty() && !type.equalsIgnoreCase("ALL")) {
      useTypeFilter = true;
      if (type.equalsIgnoreCase("TRANSFER")) {
        docTypes.add(com.dut.erp.enums.DocumentType.TRANSFER_IN);
        docTypes.add(com.dut.erp.enums.DocumentType.TRANSFER_OUT);
      } else {
        try {
          docTypes.add(com.dut.erp.enums.DocumentType.valueOf(type.trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
          log.warn("Invalid type value: {}", type);
          useTypeFilter = false;
        }
      }
    }

    Page<UUID> ids = (search != null && !search.trim().isEmpty())
        ? inventoryDocumentRepository.findIdsByWarehouseIdAndSearch(warehouseId, search, docStatus, docTypes, useTypeFilter, pageable)
        : inventoryDocumentRepository.findIdsByWarehouseId(warehouseId, docStatus, docTypes, useTypeFilter, pageable);

    if (ids.isEmpty()) {
      return PagedEntityResponse.from(Page.empty(pageable));
    }

    List<InventoryDocument> docs = inventoryDocumentRepository.findAllByIdIn(ids.getContent());
    Map<UUID, InventoryDocument> docMap = docs.stream()
        .collect(Collectors.toMap(InventoryDocument::getId, Function.identity()));

    List<InventoryDocumentBaseResponse> responses = ids.getContent().stream()
        .map(docMap::get)
        .filter(Objects::nonNull)
        .map(doc -> {
            String partnerName = null;
            String deliveryAddress = null;
            String orderNumber = null;
            if (doc.getReferenceType() == ReferenceType.SALES_ORDER && doc.getReferenceId() != null) {
               Order order = orderRepository.findById(doc.getReferenceId()).orElse(null);
               if (order != null) {
                   orderNumber = order.getOrderNumber();
                   if (order.getPartner() != null) {
                       partnerName = order.getPartner().getName();
                       deliveryAddress = order.getPartner().getAddress();
                   }
               }
            }

            return new InventoryDocumentBaseResponse(
            doc.getId(),
            doc.getWarehouse().getId(),
            doc.getWarehouse().getName(),
            doc.getSourceWarehouse() != null ? doc.getSourceWarehouse().getId() : null,
            doc.getSourceWarehouse() != null ? doc.getSourceWarehouse().getName() : null,
            doc.getName(),
            doc.getDocumentType(),
            doc.getReferenceType(),
            doc.getReferenceId(),
            orderNumber,
            partnerName,
            deliveryAddress,
            doc.getDocumentStatus(),
            doc.getScheduledDate(),
            doc.getDateDone(),
            doc.getCreatedAt()
        );})
        .collect(Collectors.toList());

    return PagedEntityResponse.from(new PageImpl<>(responses, pageable, ids.getTotalElements()));
  }

  @Override
  public InventoryDocumentResponse getDocumentById(UUID organizationId, UUID warehouseId, UUID documentId) {
    log.info("Fetching document {} for warehouse {}", documentId, warehouseId);
    findWarehouseByIdAndOrganizationId(warehouseId, organizationId);
    InventoryDocument doc = inventoryDocumentRepository.findByIdAndWarehouseId(documentId, warehouseId)
        .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));
    return mapToResponse(doc);
  }

  @Override
  @Transactional
  public InventoryDocumentResponse confirmDocument(UUID organizationId, UUID warehouseId, UUID documentId) {
    log.info("Confirming document {} for warehouse {}", documentId, warehouseId);
    findWarehouseByIdAndOrganizationId(warehouseId, organizationId);
    InventoryDocument doc = inventoryDocumentRepository.findByIdAndWarehouseId(documentId, warehouseId)
        .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

    confirmDocumentInternal(doc);
    return mapToResponse(doc);
  }

  @Override
  @Transactional
  public InventoryDocumentResponse completeDocument(UUID organizationId, UUID warehouseId, UUID documentId) {
    log.info("Completing document {} for warehouse {}", documentId, warehouseId);
    findWarehouseByIdAndOrganizationId(warehouseId, organizationId);
    InventoryDocument doc = inventoryDocumentRepository.findByIdAndWarehouseId(documentId, warehouseId)
        .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

    if (doc.getDocumentStatus() == DocumentStatus.COMPLETED) {
      return mapToResponse(doc);
    }

    if (doc.getDocumentStatus() != DocumentStatus.CONFIRMED && doc.getDocumentStatus() != DocumentStatus.SENT) {
      throw new BadRequestException("Document must be in CONFIRMED or SENT status to be completed. Current status: " + doc.getDocumentStatus());
    }

    if (doc.getDocumentStatus() == DocumentStatus.SENT) {
      doc.setDocumentStatus(DocumentStatus.COMPLETED);
      doc.setDateDone(Instant.now());
      doc = inventoryDocumentRepository.save(doc);
      if (doc.getDocumentType() == DocumentType.ISSUE && doc.getReferenceType() == ReferenceType.SALES_ORDER && doc.getReferenceId() != null) {
        checkAndCompleteOrder(doc.getReferenceId());
      }
      return mapToResponse(doc);
    }

    boolean hasPositiveAdjustment = false;
    boolean stockChanged = false;
    if (doc.getDocumentType() == DocumentType.RECEIPT) {
      addBalance(doc.getLines(), doc.getWarehouse().getId());
      stockChanged = true;
    } else if (doc.getDocumentType() == DocumentType.TRANSFER_IN) {
      addBalance(doc.getLines(), doc.getWarehouse().getId());
      stockChanged = true;
    } else if (doc.getDocumentType() == DocumentType.ADJUSTMENT) {
      List<InventoryDocumentLine> positiveMoves = new ArrayList<>();
      List<InventoryDocumentLine> negativeMoves = new ArrayList<>();
      for (InventoryDocumentLine tx : doc.getLines()) {
        if (tx.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
          positiveMoves.add(tx);
        } else if (tx.getQuantity().compareTo(BigDecimal.ZERO) < 0) {
          negativeMoves.add(InventoryDocumentLine.builder()
              .product(tx.getProduct())
              .quantity(tx.getQuantity().negate())
              .unitCost(tx.getUnitCost())
              .valuation(tx.getQuantity().negate().multiply(tx.getUnitCost()))
              .build());
        }
      }
      if (!positiveMoves.isEmpty()) {
        addBalance(positiveMoves, doc.getWarehouse().getId());
        hasPositiveAdjustment = true;
        stockChanged = true;
      }
      if (!negativeMoves.isEmpty()) {
        deductBalance(negativeMoves, doc.getWarehouse().getId());
        stockChanged = true;
      }
    }

    if (doc.getDocumentType() == DocumentType.ISSUE || doc.getDocumentType() == DocumentType.TRANSFER_OUT) {
      throw new BadRequestException("Outbound documents must be sent first before they can be completed.");
    }

    // Calculate COGS and initialize remaining quantities
    cogsValuationEngine.calculateCOGS(doc);

    doc.setDocumentStatus(DocumentStatus.COMPLETED);
    doc.setDateDone(Instant.now());
    
    if (doc.getReferenceType() == ReferenceType.REPLENISHMENT && doc.getReferenceId() != null) {
      replenishmentRequestRepository.findById(doc.getReferenceId()).ifPresent(req -> {
        req.setStatus(ReplenishmentStatus.RESOLVED);
        replenishmentRequestRepository.save(req);
      });
    }

    InventoryDocument savedDoc = inventoryDocumentRepository.save(doc);

    if (stockChanged) {
      reevaluateWaitingDocuments(savedDoc.getWarehouse().getId());
    }

    return mapToResponse(savedDoc);
  }

  @Override
  @Transactional
  public InventoryDocumentResponse sentDocument(UUID organizationId, UUID warehouseId, UUID documentId) {
    log.info("Sending document {} for warehouse {}", documentId, warehouseId);
    findWarehouseByIdAndOrganizationId(warehouseId, organizationId);
    InventoryDocument doc = inventoryDocumentRepository.findByIdAndWarehouseId(documentId, warehouseId)
        .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

    if (doc.getDocumentStatus() == DocumentStatus.SENT) {
      return mapToResponse(doc);
    }

    if (doc.getDocumentStatus() != DocumentStatus.CONFIRMED) {
      throw new BadRequestException("Document must be in CONFIRMED status to be sent. Current status: " + doc.getDocumentStatus());
    }

    if (doc.getDocumentType() != DocumentType.ISSUE && doc.getDocumentType() != DocumentType.TRANSFER_OUT) {
      throw new BadRequestException("Only ISSUE and TRANSFER_OUT documents can be sent");
    }

    // Deduct stock balance
    deductBalance(doc.getLines(), doc.getWarehouse().getId());

    // Calculate COGS and initialize remaining quantities
    cogsValuationEngine.calculateCOGS(doc);

    if (doc.getDocumentType() == DocumentType.TRANSFER_OUT && doc.getReferenceId() != null) {
      inventoryDocumentRepository.findById(doc.getReferenceId()).ifPresent(docIn -> {
        Map<UUID, InventoryDocumentLine> outLineMap = doc.getLines().stream()
            .collect(Collectors.toMap(
                line -> line.getProduct().getId(),
                Function.identity(),
                (l1, l2) -> l1));

        for (InventoryDocumentLine lineIn : docIn.getLines()) {
          InventoryDocumentLine lineOut = outLineMap.get(lineIn.getProduct().getId());
          if (lineOut != null) {
            lineIn.setUnitCost(lineOut.getUnitCost());
            lineIn.setValuation(lineIn.getQuantity().multiply(lineOut.getUnitCost()));
          }
        }
        inventoryDocumentRepository.save(docIn);
      });
    }

    doc.setDocumentStatus(DocumentStatus.SENT);
    doc.setDateDone(Instant.now());
    InventoryDocument savedDoc = inventoryDocumentRepository.save(doc);

    // Reevaluate other waiting documents since stock decreased
    reevaluateWaitingDocuments(savedDoc.getWarehouse().getId());

    if (savedDoc.getDocumentType() == DocumentType.ISSUE 
        && savedDoc.getReferenceType() == ReferenceType.SALES_ORDER 
        && savedDoc.getReferenceId() != null) {
      checkAndCompleteOrder(savedDoc.getReferenceId());
    }

    return mapToResponse(savedDoc);
  }

  @Override
  @Transactional
  public InventoryDocumentResponse cancelDocument(UUID organizationId, UUID warehouseId, UUID documentId) {
    log.info("Cancelling document {} for warehouse {}", documentId, warehouseId);
    findWarehouseByIdAndOrganizationId(warehouseId, organizationId);
    InventoryDocument doc = inventoryDocumentRepository.findByIdAndWarehouseId(documentId, warehouseId)
        .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

    if (doc.getDocumentStatus() == DocumentStatus.COMPLETED || doc.getDocumentStatus() == DocumentStatus.CANCELLED || doc.getDocumentStatus() == DocumentStatus.SENT) {
      throw new BadRequestException("Cannot cancel a completed, sent, or already cancelled document");
    }


    doc.setDocumentStatus(DocumentStatus.CANCELLED);
    doc = inventoryDocumentRepository.save(doc);

    // Cancel linked transfer document if applicable
    if (doc.getDocumentType() == DocumentType.TRANSFER_OUT && doc.getReferenceId() != null) {
      inventoryDocumentRepository.findById(doc.getReferenceId()).ifPresent(linkedDoc -> {
        if (linkedDoc.getDocumentStatus() != DocumentStatus.COMPLETED && 
            linkedDoc.getDocumentStatus() != DocumentStatus.CANCELLED) {
          linkedDoc.setDocumentStatus(DocumentStatus.CANCELLED);
          inventoryDocumentRepository.save(linkedDoc);
        }
      });
    } else if (doc.getDocumentType() == DocumentType.TRANSFER_IN) {
      // Find linked TRANSFER_OUT by checking who points to this TRANSFER_IN
      inventoryDocumentRepository.findByReferenceTypeAndReferenceIdAndDocumentType(
          ReferenceType.MANUAL, doc.getId(), DocumentType.TRANSFER_OUT
      ).ifPresent(linkedDoc -> {
        if (linkedDoc.getDocumentStatus() != DocumentStatus.COMPLETED && 
            linkedDoc.getDocumentStatus() != DocumentStatus.CANCELLED &&
            linkedDoc.getDocumentStatus() != DocumentStatus.SENT) {
          linkedDoc.setDocumentStatus(DocumentStatus.CANCELLED);
          inventoryDocumentRepository.save(linkedDoc);
        }
      });
      // Standard fallback lookup if not replenishment
      if (doc.getReferenceType() != ReferenceType.REPLENISHMENT && doc.getReferenceId() != null) {
        inventoryDocumentRepository.findById(doc.getReferenceId()).ifPresent(linkedDoc -> {
          if (linkedDoc.getDocumentStatus() != DocumentStatus.COMPLETED && 
              linkedDoc.getDocumentStatus() != DocumentStatus.CANCELLED &&
              linkedDoc.getDocumentStatus() != DocumentStatus.SENT) {
            linkedDoc.setDocumentStatus(DocumentStatus.CANCELLED);
            inventoryDocumentRepository.save(linkedDoc);
          }
        });
      }
    }

    if (doc.getReferenceType() == ReferenceType.REPLENISHMENT && doc.getReferenceId() != null) {
      replenishmentRequestRepository.findById(doc.getReferenceId()).ifPresent(req -> {
        ReplenishmentStatus oldReqStatus = req.getStatus();
        req.setStatus(ReplenishmentStatus.CANCELED);
        replenishmentRequestRepository.save(req);
        applicationEventPublisher.publishEvent(new ReplenishmentRequestStatusChangedEvent(
            req.getId(), oldReqStatus, ReplenishmentStatus.CANCELED));
      });
    }

    // Also cancel any replenishment request created from this source document
    final UUID finalDocId = doc.getId();
    replenishmentRequestRepository.findByInventoryDocumentId(finalDocId).ifPresent(req -> {
      if (req.getStatus() != ReplenishmentStatus.CANCELED) {
        ReplenishmentStatus oldReqStatus = req.getStatus();
        req.setStatus(ReplenishmentStatus.CANCELED);
        replenishmentRequestRepository.save(req);
        applicationEventPublisher.publishEvent(new ReplenishmentRequestStatusChangedEvent(
            req.getId(), oldReqStatus, ReplenishmentStatus.CANCELED));
        log.info("Automatically cancelled replenishment request {} because its source inventory document {} was CANCELLED", req.getId(), finalDocId);
      }
    });

    if (doc.getReferenceType() == ReferenceType.SALES_ORDER) {
      Order order = orderRepository.findById(doc.getReferenceId())
          .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found"));
      order.setStatus(OrderStatus.CONFIRMED);
      orderRepository.save(order);
    }

    return mapToResponse(doc);
  }

  // ---- Private Helpers ----

  private Warehouse findWarehouseByIdAndOrganizationId(UUID warehouseId, UUID organizationId) {
    Warehouse warehouse = warehouseRepository.findByIdAndOrganizationId(warehouseId, organizationId)
        .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found: " + warehouseId));
    securityAuthService.isWarehouseStaffOrManagerOrAdmin(warehouse, SecurityUtils.getCurrentUser());
    return warehouse;
  }

  private String generateDocumentName(DocumentType type) {
    String prefix = switch (type) {
      case RECEIPT -> "WH-IN-";
      case ISSUE -> "WH-OUT-";
      case ADJUSTMENT -> "WH-ADJ-";
      case TRANSFER_IN -> "WH-TRA-IN-";
      case TRANSFER_OUT -> "WH-TRA-OUT-";
    };
    return prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
  }

  private void deductBalance(List<InventoryDocumentLine> transactions, UUID warehouseId) {
    if (transactions.isEmpty()) {
      return;
    }
    List<UUID> productIds = transactions.stream()
        .map(tx -> tx.getProduct().getId())
        .distinct()
        .collect(Collectors.toList());

    List<InventoryBalance> balances = inventoryBalanceRepository
        .findAllByWarehouseIdAndProductIdIn(warehouseId, productIds);

    Map<UUID, InventoryBalance> balanceMap = balances.stream()
        .collect(Collectors.toMap(ib -> ib.getProduct().getId(), Function.identity()));

    for (InventoryDocumentLine tx : transactions) {
      UUID productId = tx.getProduct().getId();
      InventoryBalance balance = balanceMap.get(productId);
      if (balance == null) {
        throw new ResourceNotFoundException("Inventory balance not found for product: " + tx.getProduct().getName());
      }

      if (balance.getQuantity().compareTo(tx.getQuantity()) < 0) {
        throw new BadRequestException("Insufficient stock for product: " + tx.getProduct().getName());
      }
      balance.setQuantity(balance.getQuantity().subtract(tx.getQuantity()));
    }
    inventoryBalanceRepository.saveAll(balances);
  }

  private void addBalance(List<InventoryDocumentLine> transactions, UUID warehouseId) {
    if (transactions.isEmpty()) {
      return;
    }
    List<UUID> productIds = transactions.stream()
        .map(tx -> tx.getProduct().getId())
        .distinct()
        .collect(Collectors.toList());

    List<InventoryBalance> balances = inventoryBalanceRepository
        .findAllByWarehouseIdAndProductIdIn(warehouseId, productIds);

    Map<UUID, InventoryBalance> balanceMap = balances.stream()
        .collect(Collectors.toMap(ib -> ib.getProduct().getId(), Function.identity()));

    Warehouse warehouse = null;
    List<InventoryBalance> newBalances = new ArrayList<>();

    for (InventoryDocumentLine tx : transactions) {
      UUID productId = tx.getProduct().getId();
      InventoryBalance balance = balanceMap.get(productId);
      if (balance == null) {
        if (warehouse == null) {
          warehouse = warehouseRepository.findById(warehouseId)
              .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found: " + warehouseId));
        }
        balance = InventoryBalance.builder()
            .warehouse(warehouse)
            .product(tx.getProduct())
            .quantity(BigDecimal.ZERO)
            .build();
        balanceMap.put(productId, balance);
        newBalances.add(balance);
      }
      balance.setQuantity(balance.getQuantity().add(tx.getQuantity()));
    }

    if (!newBalances.isEmpty()) {
      balances.addAll(newBalances);
    }
    inventoryBalanceRepository.saveAll(balances);
  }

  private void confirmDocumentInternal(InventoryDocument doc) {
    if (doc.getDocumentStatus() != DocumentStatus.DRAFT) {
      throw new BadRequestException("Only DRAFT documents can be confirmed");
    }

    if (doc.getDocumentType() == DocumentType.RECEIPT) {
      doc.setDocumentStatus(DocumentStatus.CONFIRMED);
      inventoryDocumentRepository.save(doc);
    } else if (doc.getDocumentType() == DocumentType.ISSUE || doc.getDocumentType() == DocumentType.TRANSFER_IN || doc.getDocumentType() == DocumentType.TRANSFER_OUT) {
      if (doc.getDocumentType() == DocumentType.TRANSFER_IN) {
        throw new BadRequestException("Inbound transfer cannot be manually confirmed. It will automatically be confirmed when the source warehouse confirms the outbound transfer.");
      }

      UUID stockWarehouseId = doc.getWarehouse().getId();

      List<UUID> productIds = doc.getLines().stream()
          .map(tx -> tx.getProduct().getId())
          .collect(Collectors.toList());
      List<InventoryBalance> balances = inventoryBalanceRepository
          .findAllByWarehouseIdAndProductIdIn(stockWarehouseId, productIds);
      Map<UUID, InventoryBalance> balanceMap = balances.stream()
          .collect(Collectors.toMap(ib -> ib.getProduct().getId(), Function.identity()));

      boolean isSufficient = true;
      for (InventoryDocumentLine tx : doc.getLines()) {
        InventoryBalance balance = balanceMap.get(tx.getProduct().getId());
        if (balance == null) {
          throw new ResourceNotFoundException("Inventory balance not found for product: " + tx.getProduct().getName());
        }
        if (balance.getQuantity().compareTo(tx.getQuantity()) < 0) {
          isSufficient = false;
        }
      }

      if (isSufficient) {
        doc.setDocumentStatus(DocumentStatus.CONFIRMED);
        inventoryDocumentRepository.save(doc);
        
        if (doc.getDocumentType() == DocumentType.ISSUE && doc.getReferenceType() == ReferenceType.SALES_ORDER && doc.getReferenceId() != null) {
          orderRepository.findById(doc.getReferenceId()).ifPresent(order -> {
            OrderStatus oldStatus = order.getStatus();
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
            applicationEventPublisher.publishEvent(new OrderStatusChangedEvent(order.getId(), oldStatus, OrderStatus.CONFIRMED));
          });
        }
        
        if (doc.getDocumentType() == DocumentType.TRANSFER_OUT && doc.getReferenceId() != null) {
          inventoryDocumentRepository.findById(doc.getReferenceId()).ifPresent(docIn -> {
            if (docIn.getDocumentStatus() == DocumentStatus.DRAFT) {
              docIn.setDocumentStatus(DocumentStatus.CONFIRMED);
              inventoryDocumentRepository.save(docIn);
            }
          });
        }
      } else {
        doc.setDocumentStatus(DocumentStatus.WAITING_FOR_STOCK);
        inventoryDocumentRepository.save(doc);

        if (doc.getDocumentType() == DocumentType.ISSUE && doc.getReferenceType() == ReferenceType.SALES_ORDER && doc.getReferenceId() != null) {
          orderRepository.findById(doc.getReferenceId()).ifPresent(order -> {
            OrderStatus oldStatus = order.getStatus();
            order.setStatus(OrderStatus.WAITING_FOR_STOCK);
            orderRepository.save(order);
            applicationEventPublisher.publishEvent(new OrderStatusChangedEvent(order.getId(), oldStatus, OrderStatus.WAITING_FOR_STOCK));
          });
        }

        if (replenishmentRequestRepository.findByInventoryDocumentId(doc.getId()).isEmpty()) {
          ReplenishmentRequest replenishmentRequest = ReplenishmentRequest.builder()
              .warehouse(doc.getWarehouse())
              .inventoryDocument(doc)
              .notes("Auto-generated replenishment on confirmation.")
              .status(ReplenishmentStatus.OPEN)
              .build();
          replenishmentRequestRepository.save(replenishmentRequest);
          applicationEventPublisher.publishEvent(new ReplenishmentRequestStatusChangedEvent(replenishmentRequest.getId(), null, ReplenishmentStatus.OPEN));
        }
      }
    } else if (doc.getDocumentType() == DocumentType.ADJUSTMENT) {
      doc.setDocumentStatus(DocumentStatus.CONFIRMED);
      inventoryDocumentRepository.save(doc);
    }
  }

  private void reevaluateWaitingDocuments(UUID warehouseId) {
    // 1. Fetch all open documents (CONFIRMED and WAITING_FOR_STOCK)
    List<InventoryDocument> openDocs = new ArrayList<>();
    openDocs.addAll(inventoryDocumentRepository.findAllByWarehouseIdAndDocumentStatus(warehouseId, DocumentStatus.CONFIRMED));
    openDocs.addAll(inventoryDocumentRepository.findAllByWarehouseIdAndDocumentStatus(warehouseId, DocumentStatus.WAITING_FOR_STOCK));

    if (openDocs.isEmpty()) {
      return;
    }

    // 2. Fetch current physical balances for the warehouse
    List<UUID> productIds = openDocs.stream()
        .flatMap(doc -> doc.getLines().stream())
        .map(tx -> tx.getProduct().getId())
        .distinct()
        .collect(Collectors.toList());

    List<InventoryBalance> balances = inventoryBalanceRepository
        .findAllByWarehouseIdAndProductIdIn(warehouseId, productIds);
    Map<UUID, BigDecimal> physicalStockMap = balances.stream()
        .collect(Collectors.toMap(ib -> ib.getProduct().getId(), InventoryBalance::getQuantity));

    List<InventoryDocument> updatedDocs = new ArrayList<>();
    List<Order> updatedOrders = new ArrayList<>();
    List<ReplenishmentRequest> updatedReplenishments = new ArrayList<>();

    for (InventoryDocument doc : openDocs) {
      // We only care about outbound documents where stock availability matters
      if (doc.getDocumentType() != DocumentType.ISSUE && doc.getDocumentType() != DocumentType.TRANSFER_OUT) {
        continue;
      }

      boolean isSufficient = true;
      for (InventoryDocumentLine tx : doc.getLines()) {
        BigDecimal physicalQty = physicalStockMap.getOrDefault(tx.getProduct().getId(), BigDecimal.ZERO);
        if (physicalQty.compareTo(tx.getQuantity()) < 0) {
          isSufficient = false;
          break;
        }
      }

      DocumentStatus oldStatus = doc.getDocumentStatus();
      if (isSufficient) {
        if (oldStatus != DocumentStatus.CONFIRMED) {
          doc.setDocumentStatus(DocumentStatus.CONFIRMED);
          updatedDocs.add(doc);
          
          if (doc.getDocumentType() == DocumentType.TRANSFER_OUT && doc.getReferenceId() != null) {
            inventoryDocumentRepository.findById(doc.getReferenceId()).ifPresent(docIn -> {
              if (docIn.getDocumentStatus() == DocumentStatus.DRAFT) {
                docIn.setDocumentStatus(DocumentStatus.CONFIRMED);
                inventoryDocumentRepository.save(docIn);
              }
            });
          }

          if (doc.getReferenceType() == ReferenceType.SALES_ORDER) {
            Order order = orderRepository.findById(doc.getReferenceId())
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found"));
            order.setStatus(OrderStatus.SENT);
            updatedOrders.add(order);
          }

          replenishmentRequestRepository.findByInventoryDocumentId(doc.getId())
              .ifPresent(req -> {
                if (req.getStatus() != ReplenishmentStatus.RESOLVED) {
                  req.setStatus(ReplenishmentStatus.RESOLVED);
                  updatedReplenishments.add(req);
                  applicationEventPublisher.publishEvent(new ReplenishmentRequestStatusChangedEvent(
                      req.getId(), ReplenishmentStatus.OPEN, ReplenishmentStatus.RESOLVED));
                }
              });
        }
      } else {
        if (oldStatus != DocumentStatus.WAITING_FOR_STOCK) {
          doc.setDocumentStatus(DocumentStatus.WAITING_FOR_STOCK);
          updatedDocs.add(doc);

          if (doc.getReferenceType() == ReferenceType.SALES_ORDER) {
            Order order = orderRepository.findById(doc.getReferenceId())
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found"));
            order.setStatus(OrderStatus.WAITING_FOR_STOCK);
            updatedOrders.add(order);
          }

          // If replenishment request exists, re-open it. If not, create a new one.
          var existingReqOpt = replenishmentRequestRepository.findByInventoryDocumentId(doc.getId());
          if (existingReqOpt.isPresent()) {
            ReplenishmentRequest req = existingReqOpt.get();
            if (req.getStatus() != ReplenishmentStatus.OPEN) {
              req.setStatus(ReplenishmentStatus.OPEN);
              updatedReplenishments.add(req);
              applicationEventPublisher.publishEvent(new ReplenishmentRequestStatusChangedEvent(
                  req.getId(), ReplenishmentStatus.RESOLVED, ReplenishmentStatus.OPEN));
            }
          } else {
            ReplenishmentRequest replenishmentRequest = ReplenishmentRequest.builder()
                .warehouse(doc.getWarehouse())
                .inventoryDocument(doc)
                .notes("Auto-generated replenishment on stock depletion.")
                .status(ReplenishmentStatus.OPEN)
                .build();
            replenishmentRequestRepository.save(replenishmentRequest);
            applicationEventPublisher.publishEvent(new ReplenishmentRequestStatusChangedEvent(
                replenishmentRequest.getId(), null, ReplenishmentStatus.OPEN));
          }
        }
      }
    }

    if (!updatedDocs.isEmpty()) {
      inventoryDocumentRepository.saveAll(updatedDocs);
    }
    if (!updatedOrders.isEmpty()) {
      orderRepository.saveAll(updatedOrders);
    }
    if (!updatedReplenishments.isEmpty()) {
      replenishmentRequestRepository.saveAll(updatedReplenishments);
    }
  }

  private InventoryDocumentResponse mapToResponse(InventoryDocument doc) {
    List<InventoryDocumentLineResponse> lines = doc.getLines().stream()
        .map(move -> {
            java.math.BigDecimal estimatedCost = move.getUnitCost();
            java.math.BigDecimal estimatedValuation = move.getValuation();
            
            if (doc.getDocumentStatus() == DocumentStatus.DRAFT || 
                doc.getDocumentStatus() == DocumentStatus.CONFIRMED || 
                doc.getDocumentStatus() == DocumentStatus.WAITING_FOR_STOCK) {
                if (doc.getDocumentType() == DocumentType.ISSUE || 
                    doc.getDocumentType() == DocumentType.TRANSFER_OUT || 
                    (doc.getDocumentType() == DocumentType.ADJUSTMENT && move.getQuantity().compareTo(java.math.BigDecimal.ZERO) < 0)) {
                    estimatedCost = cogsValuationEngine.estimateUnitCost(
                        move.getProduct(), 
                        doc.getWarehouse().getId(), 
                        move.getQuantity()
                    );
                    estimatedValuation = move.getQuantity().abs().multiply(estimatedCost);
                }
            }
            
            return new InventoryDocumentLineResponse(
                move.getId(),
                move.getProduct().getId(),
                move.getProduct().getName(),
                move.getQuantity(),
                estimatedCost,
                estimatedValuation
            );
        })
        .collect(Collectors.toList());

    UserBaseResponse createdByResp = doc.getCreatedBy() != null
        ? new UserBaseResponse(doc.getCreatedBy().getId(), doc.getCreatedBy().getEmail(), doc.getCreatedBy().getFirstName(), doc.getCreatedBy().getLastName())
        : null;

    UserBaseResponse updatedByResp = doc.getUpdatedBy() != null
        ? new UserBaseResponse(doc.getUpdatedBy().getId(), doc.getUpdatedBy().getEmail(), doc.getUpdatedBy().getFirstName(), doc.getUpdatedBy().getLastName())
        : null;

    boolean hasActiveReplenishment = false;
    UUID replenishmentRequestId = null;
    if (doc.getDocumentStatus() == DocumentStatus.WAITING_FOR_STOCK) {
      var activeRepl = replenishmentRequestRepository
          .findByInventoryDocumentId(doc.getId())
          .filter(req -> req.getStatus() == ReplenishmentStatus.OPEN);
      hasActiveReplenishment = activeRepl.isPresent();
      if (hasActiveReplenishment) {
        replenishmentRequestId = activeRepl.get().getId();
      }
    } else if (doc.getReferenceType() == ReferenceType.REPLENISHMENT) {
      replenishmentRequestId = doc.getReferenceId();
    }

    String partnerName = null;
    String deliveryAddress = null;
    String orderNumber = null;
    if (doc.getReferenceType() == ReferenceType.SALES_ORDER && doc.getReferenceId() != null) {
        Order order = orderRepository.findById(doc.getReferenceId()).orElse(null);
        if (order != null) {
            orderNumber = order.getOrderNumber();
            if (order.getPartner() != null) {
                partnerName = order.getPartner().getName();
                deliveryAddress = order.getPartner().getAddress();
            }
        }
    }

    return new InventoryDocumentResponse(
        doc.getId(),
        doc.getWarehouse().getId(),
        doc.getWarehouse().getName(),
        doc.getSourceWarehouse() != null ? doc.getSourceWarehouse().getId() : null,
        doc.getSourceWarehouse() != null ? doc.getSourceWarehouse().getName() : null,
        doc.getName(),
        doc.getDocumentType(),
        doc.getReferenceType(),
        doc.getReferenceId(),
        orderNumber,
        partnerName,
        deliveryAddress,
        doc.getDocumentStatus(),
        doc.getNotes(),
        doc.getScheduledDate(),
        doc.getDateDone(),
        lines,
        doc.getCreatedAt(),
        doc.getUpdatedAt(),
        createdByResp,
        updatedByResp,
        hasActiveReplenishment,
        replenishmentRequestId
    );
  }

  private void checkAndCompleteOrder(UUID orderId) {
    Order order = orderRepository.findById(orderId).orElse(null);
    if (order == null) return;

    // 1. Check if the linked invoice is paid
    boolean isInvoicePaid = invoiceRepository.findByOrderIdAndOrganizationId(order.getId(), order.getOrganization().getId())
        .map(inv -> inv.getStatus() == InvoiceStatus.PAID)
        .orElse(false);

    // 2. Check if the active warehouse issue document is completed
    boolean isDeliveryCompleted = inventoryDocumentRepository
        .findByReferenceTypeAndReferenceIdAndDocumentType(
            ReferenceType.SALES_ORDER, order.getId(), DocumentType.ISSUE)
        .map(doc -> doc.getDocumentStatus() == DocumentStatus.COMPLETED || doc.getDocumentStatus() == DocumentStatus.SENT)
        .orElse(false);

    // 3. If both are completed/paid -> Close the order
    if (isInvoicePaid && isDeliveryCompleted) {
      OrderStatus oldStatus = order.getStatus();
      if (oldStatus != OrderStatus.COMPLETED) {
        order.setStatus(OrderStatus.COMPLETED);
        if (order.getLead() != null && order.getLead().getStage() != LeadStage.WON) {
          order.getLead().setStage(LeadStage.WON);
        }
        orderRepository.save(order);
        log.info("Automatically completed order {} because both delivery and payment are completed.", order.getOrderNumber());
        applicationEventPublisher.publishEvent(new OrderStatusChangedEvent(order.getId(), oldStatus, OrderStatus.COMPLETED));
      }
    } else if (isDeliveryCompleted) {
      OrderStatus oldStatus = order.getStatus();
      if (oldStatus != OrderStatus.SENT && oldStatus != OrderStatus.COMPLETED) {
        order.setStatus(OrderStatus.SENT);
        orderRepository.save(order);
        log.info("Order {} transitioned to SENT status because delivery is sent but invoice is not paid.", order.getOrderNumber());
        applicationEventPublisher.publishEvent(new OrderStatusChangedEvent(order.getId(), oldStatus, OrderStatus.SENT));
      }
    }
  }
}
