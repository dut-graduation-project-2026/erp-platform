package com.dut.erp.service.impl;

import com.dut.erp.constant.SortingConstants;
import com.dut.erp.dto.common.SortField;
import com.dut.erp.dto.request.CreateInventoryDocumentRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.response.InventoryDocumentBaseResponse;
import com.dut.erp.dto.response.InventoryDocumentResponse;
import com.dut.erp.dto.response.InventoryTransactionResponse;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.UserBaseResponse;
import com.dut.erp.entity.InventoryBalance;
import com.dut.erp.entity.InventoryDocument;
import com.dut.erp.entity.InventoryTransaction;
import com.dut.erp.entity.Order;
import com.dut.erp.entity.OrderItem;
import com.dut.erp.entity.Product;
import com.dut.erp.entity.Warehouse;
import com.dut.erp.enums.DocumentStatus;
import com.dut.erp.enums.DocumentType;
import com.dut.erp.enums.OrderStatus;
import com.dut.erp.enums.ReferenceType;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.repository.InventoryBalanceRepository;
import com.dut.erp.repository.InventoryDocumentRepository;
import com.dut.erp.repository.InventoryTransactionRepository;
import com.dut.erp.repository.OrderRepository;
import com.dut.erp.repository.ProductRepository;
import com.dut.erp.repository.ReplenishmentRequestRepository;
import com.dut.erp.repository.WarehouseRepository;
import com.dut.erp.service.InventoryDocumentService;
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
  private final InventoryTransactionRepository inventoryTransactionRepository;
  private final InventoryBalanceRepository inventoryBalanceRepository;
  private final OrderRepository orderRepository;
  private final ReplenishmentRequestRepository replenishmentRequestRepository;

  @Override
  @Transactional
  public InventoryDocumentResponse createDocument(
      UUID organizationId, UUID warehouseId, CreateInventoryDocumentRequest request) {
    log.info("Creating manual inventory document of type {} in warehouse {}", request.documentType(), warehouseId);
    Warehouse warehouse = findWarehouseByIdAndOrganizationId(warehouseId, organizationId);

    Warehouse sourceWarehouse = null;
    if (request.documentType() == DocumentType.TRANSFER) {
      if (request.sourceWarehouseId() == null) {
        throw new BadRequestException("Source warehouse is required for TRANSFER document type");
      }
      if (request.sourceWarehouseId().equals(warehouseId)) {
        throw new BadRequestException("Source and destination warehouses cannot be the same");
      }
      sourceWarehouse = findWarehouseByIdAndOrganizationId(request.sourceWarehouseId(), organizationId);
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

    List<InventoryTransaction> moves = request.items().stream()
        .map(item -> {
          Product product = productRepository.findByIdAndOrganizationId(item.productId(), organizationId)
              .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + item.productId()));
          return InventoryTransaction.builder()
              .inventoryDocument(doc)
              .product(product)
              .quantity(item.quantity())
              .build();
        })
        .collect(Collectors.toList());

    doc.setStockMoves(moves);
    InventoryDocument savedDoc = inventoryDocumentRepository.save(doc);

    return mapToResponse(savedDoc);
  }

  @Override
  @Transactional
  public InventoryDocumentResponse claimOrder(UUID organizationId, UUID warehouseId, UUID orderId) {
    log.info("Claiming sales order {} for warehouse {}", orderId, warehouseId);
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

    List<InventoryTransaction> moves = new ArrayList<>();
    for (OrderItem item : order.getItems()) {
      moves.add(InventoryTransaction.builder()
          .inventoryDocument(doc)
          .product(item.getProduct())
          .quantity(item.getQuantity())
          .build());
    }
    doc.setStockMoves(moves);

    // Stock check
    boolean isSufficient = true;
    for (InventoryTransaction tx : moves) {
      InventoryBalance balance = inventoryBalanceRepository
          .findByWarehouseIdAndProductId(warehouseId, tx.getProduct().getId())
          .orElseThrow(() -> new ResourceNotFoundException("Inventory balance not found for product: " + tx.getProduct().getName()));
      if (balance.getQuantity().compareTo(tx.getQuantity()) < 0) {
        isSufficient = false;
      }
    }

    if (isSufficient) {
      doc.setDocumentStatus(DocumentStatus.CONFIRMED);
      deductBalance(moves, warehouseId);
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
  public InventoryDocumentResponse completeDocument(UUID organizationId, UUID warehouseId, UUID documentId) {
    log.info("Completing document {} for warehouse {}", documentId, warehouseId);
    InventoryDocument doc = inventoryDocumentRepository.findByIdAndWarehouseId(documentId, warehouseId)
        .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

    if (doc.getDocumentStatus() == DocumentStatus.DRAFT) {
      confirmDocumentInternal(doc);
    }

    if (doc.getDocumentStatus() == DocumentStatus.WAITING_FOR_STOCK) {
      throw new BadRequestException("Cannot complete document in WAITING_FOR_STOCK status");
    }

    if (doc.getDocumentStatus() == DocumentStatus.COMPLETED) {
      return mapToResponse(doc);
    }

    if (doc.getDocumentStatus() != DocumentStatus.CONFIRMED) {
      throw new BadRequestException("Document must be in CONFIRMED status to be completed");
    }

    if (doc.getDocumentType() == DocumentType.RECEIPT) {
      BigDecimal totalIncomingQty = doc.getStockMoves().stream()
          .map(InventoryTransaction::getQuantity)
          .reduce(BigDecimal.ZERO, BigDecimal::add);

      Warehouse wh = doc.getWarehouse();
      BigDecimal currentUsed = wh.getUsedCapacity() != null ? wh.getUsedCapacity() : BigDecimal.ZERO;
      BigDecimal maxCap = wh.getMaximumCapacity() != null ? wh.getMaximumCapacity() : BigDecimal.ZERO;

      boolean isOverCapacity = currentUsed.compareTo(maxCap) > 0;
      if (!isOverCapacity && currentUsed.add(totalIncomingQty).compareTo(maxCap) > 0) {
        boolean hasWaiting = inventoryDocumentRepository
            .findAllByWarehouseIdAndDocumentStatus(wh.getId(), DocumentStatus.WAITING_FOR_STOCK)
            .stream().anyMatch(d -> d.getDocumentType() == DocumentType.ISSUE || d.getDocumentType() == DocumentType.TRANSFER);

        if (!hasWaiting) {
          throw new BadRequestException("Warehouse maximum capacity exceeded. Action rejected.");
        }
      }

      addBalance(doc.getStockMoves(), wh.getId());

    } else if (doc.getDocumentType() == DocumentType.TRANSFER) {
      BigDecimal totalIncomingQty = doc.getStockMoves().stream()
          .map(InventoryTransaction::getQuantity)
          .reduce(BigDecimal.ZERO, BigDecimal::add);

      Warehouse wh = doc.getWarehouse();
      BigDecimal currentUsed = wh.getUsedCapacity() != null ? wh.getUsedCapacity() : BigDecimal.ZERO;
      BigDecimal maxCap = wh.getMaximumCapacity() != null ? wh.getMaximumCapacity() : BigDecimal.ZERO;

      boolean isOverCapacity = currentUsed.compareTo(maxCap) > 0;
      if (!isOverCapacity && currentUsed.add(totalIncomingQty).compareTo(maxCap) > 0) {
        boolean hasWaiting = inventoryDocumentRepository
            .findAllByWarehouseIdAndDocumentStatus(wh.getId(), DocumentStatus.WAITING_FOR_STOCK)
            .stream().anyMatch(d -> d.getDocumentType() == DocumentType.ISSUE || d.getDocumentType() == DocumentType.TRANSFER);

        if (!hasWaiting) {
          throw new BadRequestException("Warehouse maximum capacity exceeded. Action rejected.");
        }
      }

      addBalance(doc.getStockMoves(), wh.getId());
    }

    doc.setDocumentStatus(DocumentStatus.COMPLETED);
    doc.setDateDone(Instant.now());
    doc = inventoryDocumentRepository.save(doc);

    if (doc.getReferenceType() == ReferenceType.SALES_ORDER) {
      Order order = orderRepository.findById(doc.getReferenceId())
          .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found"));
      order.setStatus(OrderStatus.COMPLETED);
      orderRepository.save(order);
    }

    if (doc.getDocumentType() == DocumentType.RECEIPT || doc.getDocumentType() == DocumentType.TRANSFER) {
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
        addBalance(doc.getStockMoves(), doc.getWarehouse().getId());
      } else if (doc.getDocumentType() == DocumentType.TRANSFER) {
        addBalance(doc.getStockMoves(), doc.getSourceWarehouse().getId());
      }
    }

    doc.setDocumentStatus(DocumentStatus.CANCELLED);
    doc = inventoryDocumentRepository.save(doc);

    if (doc.getReferenceType() == ReferenceType.SALES_ORDER) {
      Order order = orderRepository.findById(doc.getReferenceId())
          .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found"));
      order.setStatus(OrderStatus.DRAFT);
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
      case TRANSFER -> "WH-TRA-";
    };
    return prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
  }

  private void deductBalance(List<InventoryTransaction> transactions, UUID warehouseId) {
    for (InventoryTransaction tx : transactions) {
      InventoryBalance balance = inventoryBalanceRepository
          .findByWarehouseIdAndProductId(warehouseId, tx.getProduct().getId())
          .orElseThrow(() -> new ResourceNotFoundException("Inventory balance not found for product: " + tx.getProduct().getName()));

      if (balance.getQuantity().compareTo(tx.getQuantity()) < 0) {
        throw new BadRequestException("Insufficient stock for product: " + tx.getProduct().getName());
      }
      balance.setQuantity(balance.getQuantity().subtract(tx.getQuantity()));
      inventoryBalanceRepository.save(balance);

      Warehouse warehouse = balance.getWarehouse();
      BigDecimal currentUsed = warehouse.getUsedCapacity() != null ? warehouse.getUsedCapacity() : BigDecimal.ZERO;
      BigDecimal newCapacity = currentUsed.subtract(tx.getQuantity());
      if (newCapacity.compareTo(BigDecimal.ZERO) < 0) {
        newCapacity = BigDecimal.ZERO;
      }
      warehouse.setUsedCapacity(newCapacity);
      warehouseRepository.save(warehouse);
    }
  }

  private void addBalance(List<InventoryTransaction> transactions, UUID warehouseId) {
    for (InventoryTransaction tx : transactions) {
      InventoryBalance balance = inventoryBalanceRepository
          .findByWarehouseIdAndProductId(warehouseId, tx.getProduct().getId())
          .orElseGet(() -> {
            Warehouse wh = warehouseRepository.findById(warehouseId).orElseThrow();
            return InventoryBalance.builder()
                .warehouse(wh)
                .product(tx.getProduct())
                .quantity(BigDecimal.ZERO)
                .build();
          });
      balance.setQuantity(balance.getQuantity().add(tx.getQuantity()));
      inventoryBalanceRepository.save(balance);

      Warehouse warehouse = balance.getWarehouse();
      BigDecimal currentUsed = warehouse.getUsedCapacity() != null ? warehouse.getUsedCapacity() : BigDecimal.ZERO;
      warehouse.setUsedCapacity(currentUsed.add(tx.getQuantity()));
      warehouseRepository.save(warehouse);
    }
  }

  private void confirmDocumentInternal(InventoryDocument doc) {
    if (doc.getDocumentStatus() != DocumentStatus.DRAFT) {
      throw new BadRequestException("Only DRAFT documents can be confirmed");
    }

    if (doc.getDocumentType() == DocumentType.RECEIPT) {
      doc.setDocumentStatus(DocumentStatus.CONFIRMED);
      inventoryDocumentRepository.save(doc);
    } else if (doc.getDocumentType() == DocumentType.ISSUE || doc.getDocumentType() == DocumentType.TRANSFER) {
      UUID stockWarehouseId = (doc.getDocumentType() == DocumentType.TRANSFER)
          ? doc.getSourceWarehouse().getId()
          : doc.getWarehouse().getId();

      boolean isSufficient = true;
      for (InventoryTransaction tx : doc.getStockMoves()) {
        InventoryBalance balance = inventoryBalanceRepository
            .findByWarehouseIdAndProductId(stockWarehouseId, tx.getProduct().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Inventory balance not found for product: " + tx.getProduct().getName()));
        if (balance.getQuantity().compareTo(tx.getQuantity()) < 0) {
          isSufficient = false;
        }
      }

      if (isSufficient) {
        doc.setDocumentStatus(DocumentStatus.CONFIRMED);
        inventoryDocumentRepository.save(doc);
        deductBalance(doc.getStockMoves(), stockWarehouseId);
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

    for (InventoryDocument doc : waitingDocs) {
      UUID stockWarehouseId = (doc.getDocumentType() == DocumentType.TRANSFER)
          ? doc.getSourceWarehouse().getId()
          : doc.getWarehouse().getId();

      boolean isSufficient = true;
      for (InventoryTransaction tx : doc.getStockMoves()) {
        InventoryBalance balance = inventoryBalanceRepository
            .findByWarehouseIdAndProductId(stockWarehouseId, tx.getProduct().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Inventory balance not found"));
        if (balance.getQuantity().compareTo(tx.getQuantity()) < 0) {
          isSufficient = false;
          break;
        }
      }

      if (isSufficient) {
        doc.setDocumentStatus(DocumentStatus.CONFIRMED);
        inventoryDocumentRepository.save(doc);

        deductBalance(doc.getStockMoves(), stockWarehouseId);

        if (doc.getReferenceType() == ReferenceType.SALES_ORDER) {
          Order order = orderRepository.findById(doc.getReferenceId())
              .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found"));
          order.setStatus(OrderStatus.SENT);
          orderRepository.save(order);
        }

        replenishmentRequestRepository.findByInventoryDocumentId(doc.getId())
            .ifPresent(req -> {
              req.setStatus("RESOLVED");
              replenishmentRequestRepository.save(req);
            });
      }
    }
  }

  private InventoryDocumentResponse mapToResponse(InventoryDocument doc) {
    List<InventoryTransactionResponse> moves = doc.getStockMoves().stream()
        .map(move -> new InventoryTransactionResponse(
            move.getId(),
            move.getProduct().getId(),
            move.getProduct().getName(),
            move.getQuantity()))
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
        moves,
        doc.getCreatedAt(),
        doc.getUpdatedAt(),
        createdByResp,
        updatedByResp
    );
  }
}
