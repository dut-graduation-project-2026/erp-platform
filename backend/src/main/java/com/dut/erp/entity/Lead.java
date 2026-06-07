package com.dut.erp.entity;

import com.dut.erp.enums.LeadStage;
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
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "leads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Lead {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false, updatable = false)
  UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "organization_id", nullable = false)
  Organization organization;

  @Column(name = "name", nullable = false, length = 255)
  String name;

  @Column(name = "tax_code", length = 50)
  String taxCode;

  @Column(name = "email", length = 255)
  String email;

  @Column(name = "phone", length = 50)
  String phone;

  @Column(name = "address", length = 255)
  String address;

  @Column(name = "notes", columnDefinition = "TEXT")
  String notes;

  @Column(name = "expected_revenue", precision = 15, scale = 2)
  @Builder.Default
  BigDecimal expectedRevenue = BigDecimal.ZERO;

  @Column(name = "stage", length = 50)
  @Enumerated(EnumType.STRING)
  @Builder.Default
  LeadStage stage = LeadStage.NEW;

  @Column(name = "probability", precision = 5, scale = 2)
  BigDecimal probability;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sale_person_id")
  User salePerson;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sale_team_id")
  SaleTeam saleTeam;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "partner_id")
  Partner partner;

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  Instant updatedAt;

  @CreatedBy
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by", updatable = false)
  User createdBy;

  @LastModifiedBy
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "updated_by")
  User updatedBy;
}
