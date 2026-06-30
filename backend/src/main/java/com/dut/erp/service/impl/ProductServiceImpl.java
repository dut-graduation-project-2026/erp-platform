package com.dut.erp.service.impl;

import com.dut.erp.constant.SortingConstants;
import com.dut.erp.dto.common.SortField;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpsertProductRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.ProductBaseResponse;
import com.dut.erp.dto.response.ProductResponse;
import com.dut.erp.entity.InventoryBalance;
import com.dut.erp.entity.Organization;
import com.dut.erp.entity.Product;
import com.dut.erp.entity.ProductCategory;
import com.dut.erp.entity.Warehouse;
import com.dut.erp.entity.OrderItem;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.ProductMapper;
import com.dut.erp.repository.InventoryBalanceRepository;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.repository.ProductCategoryRepository;
import com.dut.erp.repository.ProductRepository;
import com.dut.erp.repository.WarehouseRepository;
import com.dut.erp.repository.OrderItemRepository;
import com.dut.erp.repository.OrderRepository;
import com.dut.erp.service.ProductService;
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
public class ProductServiceImpl implements ProductService {

  private final OrganizationRepository organizationRepository;
  private final ProductRepository productRepository;
  private final ProductCategoryRepository productCategoryRepository;
  private final WarehouseRepository warehouseRepository;
  private final InventoryBalanceRepository inventoryBalanceRepository;
  private final OrderItemRepository orderItemRepository;
  private final OrderRepository orderRepository;
  private final ProductMapper productMapper;

  @Override
  public PagedEntityResponse<ProductBaseResponse> getProductsWithFilterByOrganizationId(
      UUID organizationId, String search, boolean isArchived, PaginationRequest paginationRequest) {
    log.info("Fetching products for organization {}", organizationId);

    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit(),
            SortingConstants.customEntitiesSort(SortField.asc("name"), SortField.asc("updatedAt")));

    Page<UUID> ids =
        (search != null && !search.trim().isEmpty())
            ? productRepository.findIdsByOrganizationIdAndIsArchivedAndSearch(organizationId, isArchived, search, pageable)
            : productRepository.findIdsByOrganizationIdAndIsArchived(organizationId, isArchived, pageable);

    if (ids.isEmpty()) {
      return PagedEntityResponse.from(Page.empty(pageable));
    }

    Map<UUID, Product> productMap =
        productRepository.findAllByIdIn(ids.getContent()).stream()
            .collect(Collectors.toMap(Product::getId, Function.identity()));

    List<ProductBaseResponse> responses =
        ids.getContent().stream()
            .map(productMap::get)
            .filter(Objects::nonNull)
            .map(productMapper::toBaseResponse)
            .collect(Collectors.toList());

    return PagedEntityResponse.from(new PageImpl<>(responses, pageable, ids.getTotalElements()));
  }

  @Override
  public ProductResponse getProductById(UUID organizationId, UUID productId) {
    log.info("Fetching product {} for organization {}", productId, organizationId);
    Product product = findProductByIdAndOrganizationId(productId, organizationId);
    return productMapper.toResponse(product);
  }

  @Override
  @Transactional
  public ProductResponse createProduct(UUID organizationId, UpsertProductRequest request) {
    Organization organization = findOrganizationById(organizationId);
    ProductCategory category = productCategoryRepository.findByIdAndOrganizationId(request.categoryId(), organizationId)
        .orElseThrow(() -> new ResourceNotFoundException("Product category not found with id: " + request.categoryId()));

    if (productRepository.existsByOrganizationIdAndSkuIgnoreCase(organizationId, request.sku().trim())) {
      throw new com.dut.erp.exception.BadRequestException("SKU '" + request.sku() + "' is already in use in this organization.");
    }

    Product product =
        Product.builder()
            .organization(organization)
            .category(category)
            .name(request.name())
            .sku(request.sku().trim().toUpperCase())
            .purchasePrice(request.purchasePrice())
            .salesPrice(request.salesPrice())
            .description(request.description())
            .cogsMethod(request.cogsMethod() != null ? request.cogsMethod() : com.dut.erp.enums.CogsMethod.FIFO)
            .image(request.image())
            .build();

    product = productRepository.save(product);
    log.info("Created product {} in organization {}", product.getId(), organizationId);

    // Seed a zero-quantity InventoryBalance for every warehouse in the organization
    List<Warehouse> warehouses = warehouseRepository.findAllByOrganizationId(organizationId);
    if (!warehouses.isEmpty()) {
      final Product savedProduct = product;
      List<InventoryBalance> balances = warehouses.stream()
          .map(warehouse -> InventoryBalance.builder()
              .warehouse(warehouse)
              .product(savedProduct)
              .build())
          .collect(Collectors.toList());
      inventoryBalanceRepository.saveAll(balances);
      log.info("Seeded {} inventory balance(s) for product {} across warehouses",
          balances.size(), savedProduct.getId());
    }

    return productMapper.toResponse(product);
  }

  @Override
  @Transactional
  public ProductResponse updateProduct(
      UUID organizationId, UUID productId, UpsertProductRequest request) {
    Product product = findProductByIdAndOrganizationId(productId, organizationId);
    ProductCategory category = productCategoryRepository.findByIdAndOrganizationId(request.categoryId(), organizationId)
        .orElseThrow(() -> new ResourceNotFoundException("Product category not found with id: " + request.categoryId()));

    if (productRepository.existsByOrganizationIdAndSkuIgnoreCaseAndIdNot(organizationId, request.sku().trim(), productId)) {
      throw new com.dut.erp.exception.BadRequestException("SKU '" + request.sku() + "' is already in use by another product in this organization.");
    }

    product.setName(request.name());
    product.setSku(request.sku().trim().toUpperCase());
    product.setPurchasePrice(request.purchasePrice());
    product.setSalesPrice(request.salesPrice());
    product.setDescription(request.description());
    product.setCategory(category);
    product.setCogsMethod(request.cogsMethod() != null ? request.cogsMethod() : com.dut.erp.enums.CogsMethod.FIFO);
    product.setImage(request.image());

    product = productRepository.save(product);
    log.info("Updated product {} in organization {}", productId, organizationId);
    return productMapper.toResponse(product);
  }

  @Override
  @Transactional
  public ProductResponse updateProductArchiveStatus(
      UUID organizationId, UUID productId, boolean isArchived) {
    Product product = findProductByIdAndOrganizationId(productId, organizationId);
    product.setArchived(isArchived);
    product = productRepository.save(product);
    log.info("Updated archive status for product {} in organization {}", productId, organizationId);

    if (isArchived) {
      List<OrderItem> itemsToDelete = orderItemRepository.findByProductIdAndOrderStatus(productId, com.dut.erp.enums.OrderStatus.DRAFT);
      if (!itemsToDelete.isEmpty()) {
        java.util.Set<com.dut.erp.entity.Order> ordersToRecalculate = itemsToDelete.stream()
            .map(OrderItem::getOrder)
            .collect(Collectors.toSet());

        orderItemRepository.deleteAll(itemsToDelete);

        for (com.dut.erp.entity.Order order : ordersToRecalculate) {
          BigDecimal newTotal = orderItemRepository.sumSubtotalByOrderId(order.getId());
          order.setTotalAmount(newTotal);
          orderRepository.save(order);
          log.info("Recalculated totalAmount for draft order {} to {}", order.getOrderNumber(), newTotal);
        }
      }
    }

    return productMapper.toResponse(product);
  }

  @Override
  @Transactional
  public void deleteProduct(UUID organizationId, UUID productId) {
    Product product = findProductByIdAndOrganizationId(productId, organizationId);
    productRepository.delete(product);
    log.info("Deleted product {} from organization {}", productId, organizationId);
  }

  // ---- Private helpers ----

  private Organization findOrganizationById(UUID organizationId) {
    return organizationRepository
        .findById(organizationId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException("Organization not found with id: " + organizationId));
  }

  private Product findProductByIdAndOrganizationId(UUID productId, UUID organizationId) {
    return productRepository
        .findByIdAndOrganizationId(productId, organizationId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Product not found with id: " + productId));
  }
}
