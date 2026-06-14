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

  @Override
  @Transactional
  public InventoryDocumentResponse createDocument(
      UUID organizationId, UUID warehouseId, CreateInventoryDocumentRequest request) {
    log.info("Creating manual inventory document of type {} in warehouse {}", request.documentType(), warehouseId);

    // Validate quantities based on document type
    for (var item : request.items()) {
      if (request.documentType() == DocumentType.ADJUSTMENT) {
        if (item.quantity().compareTo(BigDecimal.ZERO) == 0) {
          throw new BadRequestException("Quantity cannot be zero for ADJUSTMENT");
        }
      } else {
        if (item.quantity().compareTo(BigDecimal.ZERO) <= 0) {
          throw new BadRequestException("Quantity must be positive for " + request.documentType());
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
      throw new ResourceNotFoundException("Product(s) not found or not in organization: " + missingIds);
    }
    Map<UUID, Product> productMap = products.stream()
        .collect(Collectors.toMap(Product::getId, Function.identity()));

    Warehouse sourceWarehouse = null;
    if (request.documentType() == DocumentType.TRANSFER_IN || request.documentType() == DocumentType.TRANSFER_OUT) {
      if (request.transferSourceWarehouseId() == null) {
        throw new BadRequestException("Source/Destination warehouse is required for TRANSFER document type");
      }
      if (request.transferSourceWarehouseId().equals(warehouseId)) {
        throw new BadRequestException("Source and destination warehouses cannot be the same");
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

      // Create docIn (Inbound Transfer at destination warehouse)
      InventoryDocument docIn = InventoryDocument.builder()
          .warehouse(destWh)
          .sourceWarehouse(sourceWh)
          .name(generateDocumentName(DocumentType.TRANSFER_IN))
          .documentType(DocumentType.TRANSFER_IN)
          .referenceType(ReferenceType.MANUAL)
          .documentStatus(DocumentStatus.DRAFT)
          .scheduledDate(request.scheduledDate())
          .notes(request.notes())
          .build();

      List<InventoryDocumentLine> linesIn = new ArrayList<>();
      for (var item : request.items()) {
        Product product = productMap.get(item.productId());
        linesIn.add(InventoryDocumentLine.builder()
            .inventoryDocument(docIn)
            .product(product)
            .quantity(item.quantity())
            .unitCost(product.getPrice())
            .valuation(item.quantity().multiply(product.getPrice()))
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
          .notes(request.notes())
          .build();

      List<InventoryDocumentLine> linesOut = new ArrayList<>();
      for (var item : request.items()) {
        Product product = productMap.get(item.productId());
        linesOut.add(InventoryDocumentLine.builder()
            .inventoryDocument(docOut)
            .product(product)
            .quantity(item.quantity())
            .unitCost(product.getPrice())
            .valuation(item.quantity().multiply(product.getPrice()))
            .build());
      }
      docOut.setLines(linesOut);
      InventoryDocument savedDocOut = inventoryDocumentRepository.save(docOut);

      // Link docIn back to docOut
      savedDocIn.setReferenceId(savedDocOut.getId());
      savedDocIn = inventoryDocumentRepository.save(savedDocIn);

      if (request.documentType() == DocumentType.TRANSFER_IN) {
        return mapToResponse(savedDocIn);
      } else {
        return mapToResponse(savedDocOut);
      }
    }

    InventoryDocument doc = InventoryDocument.builder()
        .warehouse(warehouse)
        .sourceWarehouse(sourceWarehouse)
        .name(generateDocumentName(request.documentType()))
        .documentType(request.documentType())
        .referenceType(ReferenceType.MANUAL)
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
              .unitCost(product.getPrice())
              .valuation(item.quantity().multiply(product.getPrice()))
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
        .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    if (!order.getOrganization().getId().equals(organizationId)) {
      throw new BadRequestException("Order does not belong to the requested organization");
    }

    if (order.getStatus() != OrderStatus.CONFIRMED) {
      throw new BadRequestException("Only CONFIRMED orders can be claimed");
    }

    // Check duplicate claim
    boolean alreadyClaimed = inventoryDocumentRepository
        .existsByReferenceTypeAndReferenceIdAndDocumentTypeAndDocumentStatusNot(
            ReferenceType.SALES_ORDER, orderId, DocumentType.ISSUE, DocumentStatus.CANCELLED);
    if (alreadyClaimed) {
      throw new BadRequestException("Order has already been claimed");
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
      BigDecimal productPrice = item.getProduct().getPrice();
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
        throw new ResourceNotFoundException("Inventory balance not found for product: " + tx.getProduct().getName());
      }
      if (balance.getQuantity().compareTo(tx.getQuantity()) < 0) {
        isSufficient = false;
      }
    }

    if (isSufficient) {
      doc.setDocumentStatus(DocumentStatus.CONFIRMED);
      deductBalance(lines, warehouseId);
      order.setStatus(OrderStatus.SENT);
    } else {
      doc.setDocumentStatus(DocumentStatus.WAITING_FOR_STOCK);
      order.setStatus(OrderStatus.WAITING_FOR_STOCK);
    }

    orderRepository.save(order);
    doc = inventoryDocumentRepository.save(doc);

    return mapToResponse(doc);
  }

  @Override
  public PagedEntityResponse<InventoryDocumentBaseResponse> getDocuments(
      UUID organizationId, UUID warehouseId, String search, PaginationRequest paginationRequest) {
    log.info("Fetching documents for warehouse {}", warehouseId);

    Pageable pageable = PageRequest.of(
        paginationRequest.page() - 1,
        paginationRequest.limit(),
        SortingConstants.customEntitiesSort(SortField.desc("name"), SortField.asc("createdAt"))
    );

    Page<UUID> ids = (search != null && !search.trim().isEmpty())
        ? inventoryDocumentRepository.findIdsByWarehouseIdAndSearch(warehouseId, search, pageable)
        : inventoryDocumentRepository.findIdsByWarehouseId(warehouseId, pageable);

    if (ids.isEmpty()) {
      return PagedEntityResponse.from(Page.empty(pageable));
    }

    List<InventoryDocument> docs = inventoryDocumentRepository.findAllByIdIn(ids.getContent());
    Map<UUID, InventoryDocument> docMap = docs.stream()
        .collect(Collectors.toMap(InventoryDocument::getId, Function.identity()));

    List<InventoryDocumentBaseResponse> responses = ids.getContent().stream()
        .map(docMap::get)
        .filter(Objects::nonNull)
        .map(doc -> new InventoryDocumentBaseResponse(
            doc.getId(),
            doc.getWarehouse().getId(),
            doc.getWarehouse().getName(),
            doc.getSourceWarehouse() != null ? doc.getSourceWarehouse().getId() : null,
            doc.getSourceWarehouse() != null ? doc.getSourceWarehouse().getName() : null,
            doc.getName(),
            doc.getDocumentType(),
            doc.getReferenceType(),
            doc.getReferenceId(),
            doc.getDocumentStatus(),
            doc.getScheduledDate(),
            doc.getDateDone(),
            doc.getCreatedAt()
        ))
        .collect(Collectors.toList());

    return PagedEntityResponse.from(new PageImpl<>(responses, pageable, ids.getTotalElements()));
  }

  @Override
  public InventoryDocumentResponse getDocumentById(UUID organizationId, UUID warehouseId, UUID documentId) {
    log.info("Fetching document {} for warehouse {}", documentId, warehouseId);
    InventoryDocument doc = inventoryDocumentRepository.findByIdAndWarehouseId(documentId, warehouseId)
        .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));
    return mapToResponse(doc);
  }

  @Override
  @Transactional
  public InventoryDocumentResponse confirmDocument(UUID organizationId, UUID warehouseId, UUID documentId) {
    log.info("Confirming document {} for warehouse {}", documentId, warehouseId);
    InventoryDocument doc = inventoryDocumentRepository.findByIdAndWarehouseId(documentId, warehouseId)
        .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

    confirmDocumentInternal(doc);
    return mapToResponse(doc);
  }

  @Override
  @Transactional
  public InventoryDocumentResponse completeDocument(UUID organizationId, UUID warehouseId, UUID documentId) {
    log.info("Completing document {} for warehouse {}", documentId, warehouseId);
    InventoryDocument doc = inventoryDocumentRepository.findByIdAndWarehouseId(documentId, warehouseId)
        .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

    if (doc.getDocumentStatus() == DocumentStatus.COMPLETED) {
      return mapToResponse(doc);
    }

    if (doc.getDocumentStatus() != DocumentStatus.CONFIRMED) {
      throw new BadRequestException("Document must be in CONFIRMED status to be completed. Current status: " + doc.getDocumentStatus());
    }

    boolean hasPositiveAdjustment = false;
    if (doc.getDocumentType() == DocumentType.RECEIPT) {
      addBalance(doc.getLines(), doc.getWarehouse().getId());
    } else if (doc.getDocumentType() == DocumentType.TRANSFER_IN) {
      addBalance(doc.getLines(), doc.getWarehouse().getId());
    } else if (doc.getDocumentType() == DocumentType.TRANSFER_OUT) {
      // Outbound Transfer: No addition of stock (stock was already deducted when confirmed).
      // BUT we must automatically confirm/transition the inbound document to CONFIRMED!
      if (doc.getReferenceId() != null) {
        inventoryDocumentRepository.findById(doc.getReferenceId()).ifPresent(docIn -> {
          if (docIn.getDocumentStatus() == DocumentStatus.DRAFT) {
            docIn.setDocumentStatus(DocumentStatus.CONFIRMED);
            inventoryDocumentRepository.save(docIn);
          }
        });
      }
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
      }
      if (!negativeMoves.isEmpty()) {
        deductBalance(negativeMoves, doc.getWarehouse().getId());
      }
    }

    // Calculate COGS and initialize remaining quantities
    cogsValuationEngine.calculateCOGS(doc);

    doc.setDocumentStatus(DocumentStatus.COMPLETED);
    doc.setDateDone(Instant.now());
    doc = inventoryDocumentRepository.save(doc);

    if (doc.getDocumentType() == DocumentType.RECEIPT || 
        doc.getDocumentType() == DocumentType.TRANSFER_IN || 
        (doc.getDocumentType() == DocumentType.ADJUSTMENT && hasPositiveAdjustment)) {
      reevaluateWaitingDocuments(doc.getWarehouse().getId());
    }

    return mapToResponse(doc);
  }

  @Override
  @Transactional
  public InventoryDocumentResponse cancelDocument(UUID organizationId, UUID warehouseId, UUID documentId) {
    log.info("Cancelling document {} for warehouse {}", documentId, warehouseId);
    InventoryDocument doc = inventoryDocumentRepository.findByIdAndWarehouseId(documentId, warehouseId)
        .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

    if (doc.getDocumentStatus() == DocumentStatus.COMPLETED || doc.getDocumentStatus() == DocumentStatus.CANCELLED) {
      throw new BadRequestException("Cannot cancel a completed or already cancelled document");
    }

    if (doc.getDocumentStatus() == DocumentStatus.CONFIRMED) {
      if (doc.getDocumentType() == DocumentType.ISSUE) {
        addBalance(doc.getLines(), doc.getWarehouse().getId());
      } else if (doc.getDocumentType() == DocumentType.TRANSFER_OUT) {
        // Revert stock to the source warehouse
        addBalance(doc.getLines(), doc.getWarehouse().getId());
      }
    }

    doc.setDocumentStatus(DocumentStatus.CANCELLED);
    doc = inventoryDocumentRepository.save(doc);

    // Cancel linked transfer document if applicable
    if ((doc.getDocumentType() == DocumentType.TRANSFER_IN || doc.getDocumentType() == DocumentType.TRANSFER_OUT) && doc.getReferenceId() != null) {
      inventoryDocumentRepository.findById(doc.getReferenceId()).ifPresent(linkedDoc -> {
        if (linkedDoc.getDocumentStatus() != DocumentStatus.COMPLETED && 
            linkedDoc.getDocumentStatus() != DocumentStatus.CANCELLED) {
          linkedDoc.setDocumentStatus(DocumentStatus.CANCELLED);
          inventoryDocumentRepository.save(linkedDoc);
        }
      });
    }

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
    return warehouseRepository.findByIdAndOrganizationId(warehouseId, organizationId)
        .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found: " + warehouseId));
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
        // Enforce transfer sequence safety: TRANSFER_IN can only be confirmed if TRANSFER_OUT is COMPLETED
        if (doc.getReferenceId() == null) {
          throw new BadRequestException("Inbound transfer is missing reference to outbound transfer");
        }
        InventoryDocument docOut = inventoryDocumentRepository.findById(doc.getReferenceId())
            .orElseThrow(() -> new ResourceNotFoundException("Outbound transfer document not found: " + doc.getReferenceId()));
        if (docOut.getDocumentStatus() != DocumentStatus.COMPLETED) {
          throw new BadRequestException("Cannot confirm inbound transfer before the outbound transfer is completed");
        }

        // Inbound transfers just become CONFIRMED without stock check/deduction
        doc.setDocumentStatus(DocumentStatus.CONFIRMED);
        inventoryDocumentRepository.save(doc);
        return;
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
        deductBalance(doc.getLines(), stockWarehouseId);
      } else {
        doc.setDocumentStatus(DocumentStatus.WAITING_FOR_STOCK);
        inventoryDocumentRepository.save(doc);
      }
    } else if (doc.getDocumentType() == DocumentType.ADJUSTMENT) {
      doc.setDocumentStatus(DocumentStatus.CONFIRMED);
      inventoryDocumentRepository.save(doc);
    }
  }

  private void reevaluateWaitingDocuments(UUID warehouseId) {
    List<InventoryDocument> waitingDocs = inventoryDocumentRepository
        .findAllByWarehouseIdAndDocumentStatus(warehouseId, DocumentStatus.WAITING_FOR_STOCK);
    if (waitingDocs.isEmpty()) {
      return;
    }

    List<UUID> productIds = waitingDocs.stream()
        .flatMap(doc -> doc.getLines().stream())
        .map(tx -> tx.getProduct().getId())
        .distinct()
        .collect(Collectors.toList());

    List<InventoryBalance> balances = inventoryBalanceRepository
        .findAllByWarehouseIdAndProductIdIn(warehouseId, productIds);
    Map<UUID, InventoryBalance> balanceMap = balances.stream()
        .collect(Collectors.toMap(ib -> ib.getProduct().getId(), Function.identity()));

    List<InventoryDocument> updatedDocs = new ArrayList<>();
    List<Order> updatedOrders = new ArrayList<>();
    List<ReplenishmentRequest> updatedReplenishments = new ArrayList<>();
    boolean anyDeducted = false;

    for (InventoryDocument doc : waitingDocs) {
      boolean isSufficient = true;
      for (InventoryDocumentLine tx : doc.getLines()) {
        InventoryBalance balance = balanceMap.get(tx.getProduct().getId());
        if (balance == null || balance.getQuantity().compareTo(tx.getQuantity()) < 0) {
          isSufficient = false;
          break;
        }
      }

      if (isSufficient) {
        doc.setDocumentStatus(DocumentStatus.CONFIRMED);
        updatedDocs.add(doc);

        // In-memory stock deduction
        for (InventoryDocumentLine tx : doc.getLines()) {
          InventoryBalance balance = balanceMap.get(tx.getProduct().getId());
          balance.setQuantity(balance.getQuantity().subtract(tx.getQuantity()));
        }
        anyDeducted = true;

        if (doc.getReferenceType() == ReferenceType.SALES_ORDER) {
          Order order = orderRepository.findById(doc.getReferenceId())
              .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found"));
          order.setStatus(OrderStatus.SENT);
          updatedOrders.add(order);
        }

        replenishmentRequestRepository.findByInventoryDocumentId(doc.getId())
            .ifPresent(req -> {
              req.setStatus(ReplenishmentStatus.RESOLVED);
              updatedReplenishments.add(req);
            });
      }
    }

    if (anyDeducted) {
      inventoryBalanceRepository.saveAll(balances);
    }
    if (!updatedDocs.isEmpty()) {
      inventoryDocumentRepository.saveAll(updatedDocs);
    }
    if (!updatedOrders.isEmpty()) {
      orderRepository.saveAll(updatedOrders);
    }
    if (!updatedReplenishments.isEmpty()) {
      replenishmentRequestRepository.saveAll(updatedReplenishments);
      for (ReplenishmentRequest req : updatedReplenishments) {
        applicationEventPublisher.publishEvent(new ReplenishmentRequestStatusChangedEvent(
            req.getId(), ReplenishmentStatus.OPEN, ReplenishmentStatus.RESOLVED));
      }
    }
  }

  private InventoryDocumentResponse mapToResponse(InventoryDocument doc) {
    List<InventoryDocumentLineResponse> lines = doc.getLines().stream()
        .map(move -> new InventoryDocumentLineResponse(
            move.getId(),
            move.getProduct().getId(),
            move.getProduct().getName(),
            move.getQuantity(),
            move.getUnitCost(),
            move.getValuation()))
        .collect(Collectors.toList());

    UserBaseResponse createdByResp = doc.getCreatedBy() != null
        ? new UserBaseResponse(doc.getCreatedBy().getId(), doc.getCreatedBy().getEmail(), doc.getCreatedBy().getFirstName(), doc.getCreatedBy().getLastName())
        : null;

    UserBaseResponse updatedByResp = doc.getUpdatedBy() != null
        ? new UserBaseResponse(doc.getUpdatedBy().getId(), doc.getUpdatedBy().getEmail(), doc.getUpdatedBy().getFirstName(), doc.getUpdatedBy().getLastName())
        : null;

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
        doc.getDocumentStatus(),
        doc.getNotes(),
        doc.getScheduledDate(),
        doc.getDateDone(),
        lines,
        doc.getCreatedAt(),
        doc.getUpdatedAt(),
        createdByResp,
        updatedByResp
    );
  }
}
