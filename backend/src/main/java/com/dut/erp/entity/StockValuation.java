package com.dut.erp.entity;

import com.dut.erp.enums.CostMethod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Table(name = "stock_valuation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockValuation {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false, updatable = false)
  UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  Product product;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "move_id", nullable = false)
  StockMove move;

  @Column(name = "quantity", nullable = false, precision = 15, scale = 4)
  BigDecimal quantity;

  @Column(name = "unit_value", nullable = false, precision = 15, scale = 2)
  BigDecimal unitValue;

  @Column(name = "total_value", nullable = false, precision = 15, scale = 2)
  BigDecimal totalValue;

  @Column(name = "remaining_qty", precision = 15, scale = 4)
  BigDecimal remainingQty;

  @Column(name = "remaining_value", precision = 15, scale = 2)
  BigDecimal remainingValue;

  @Enumerated(EnumType.STRING)
  @Column(name = "method", nullable = false, length = 50)
  CostMethod method;

  @Column(name = "created_at", nullable = false)
  Instant createdAt;
}
