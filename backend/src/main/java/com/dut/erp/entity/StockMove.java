package com.dut.erp.entity;

import com.dut.erp.enums.StockMoveState;
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
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "stock_move")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockMove {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false, updatable = false)
  UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "picking_id", nullable = false)
  StockPicking picking;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  Product product;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "location_id", nullable = false)
  StockLocation location;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "location_dest_id", nullable = false)
  StockLocation locationDest;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "lot_id")
  StockLot lot;

  @Column(name = "product_uom_qty", nullable = false, precision = 15, scale = 4)
  BigDecimal productUomQty;

  @Column(name = "quantity_done", nullable = false, precision = 15, scale = 4)
  @Builder.Default
  BigDecimal quantityDone = BigDecimal.ZERO;

  @Enumerated(EnumType.STRING)
  @Column(name = "state", nullable = false, length = 50)
  @Builder.Default
  StockMoveState state = StockMoveState.DRAFT;
}
