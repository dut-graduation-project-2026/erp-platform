package com.dut.erp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "inventory_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryDocumentLine {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false, updatable = false)
  UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "inventory_document_id", nullable = false)
  InventoryDocument inventoryDocument;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  Product product;

  @Column(name = "quantity", nullable = false, precision = 15, scale = 4)
  @Builder.Default
  BigDecimal quantity = BigDecimal.ZERO;

  @Column(name = "unit_cost", nullable = false, precision = 15, scale = 4)
  @Builder.Default
  BigDecimal unitCost = BigDecimal.ZERO;

  @Column(name = "valuation", nullable = false, precision = 15, scale = 4)
  @Builder.Default
  BigDecimal valuation = BigDecimal.ZERO;

  @Column(name = "remaining_quantity", nullable = false, precision = 15, scale = 4)
  @Builder.Default
  BigDecimal remainingQuantity = BigDecimal.ZERO;
}
