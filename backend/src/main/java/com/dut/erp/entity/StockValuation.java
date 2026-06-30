package com.dut.erp.entity;

import com.dut.erp.enums.CogsMethod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "stock_valuations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class StockValuation {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false, updatable = false)
  UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "inventory_document_line_id", nullable = false)
  InventoryDocumentLine inventoryDocumentLine;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  Product product;

  @Column(name = "quantity", nullable = false, precision = 15, scale = 4)
  BigDecimal quantity;

  @Column(name = "unit_cost", nullable = false, precision = 15, scale = 4)
  BigDecimal unitCost;

  @Column(name = "total_valuation", nullable = false, precision = 15, scale = 4)
  BigDecimal totalValuation;

  @Enumerated(EnumType.STRING)
  @Column(name = "method", nullable = false, length = 50)
  CogsMethod method;

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  Instant createdAt;
}
