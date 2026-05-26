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
import jakarta.persistence.UniqueConstraint;
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
@Table(
    name = "stock_quant",
    uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "location_id", "lot_id"}, name = "uk_stock_quant_prod_loc_lot")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockQuant {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false, updatable = false)
  UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  Product product;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "location_id", nullable = false)
  StockLocation location;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "lot_id")
  StockLot lot;

  @Column(name = "quantity", nullable = false, precision = 15, scale = 4)
  @Builder.Default
  BigDecimal quantity = BigDecimal.ZERO;

  @Column(name = "reserved_quantity", nullable = false, precision = 15, scale = 4)
  @Builder.Default
  BigDecimal reservedQuantity = BigDecimal.ZERO;

  @Column(name = "on_order_quantity", nullable = false, precision = 15, scale = 4)
  @Builder.Default
  BigDecimal onOrderQuantity = BigDecimal.ZERO;
}
