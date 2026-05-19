package com.dut.erp.entity;

import com.dut.erp.enums.CustomerStatus;
import com.dut.erp.enums.CustomerType;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Customer {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "organization_id", nullable = false)
  Organization organization;

  @Column(name = "code", nullable = false, length = 50)
  String code;

  @Column(name = "name", nullable = false)
  String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "customer_type", nullable = false, length = 50)
  CustomerType customerType;

  @Column(name = "tax_code", length = 50)
  String taxCode;

  @Column(name = "email")
  String email;

  @Column(name = "phone", length = 50)
  String phone;

  @Column(name = "address")
  String address;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 50)
  @Builder.Default
  CustomerStatus status = CustomerStatus.ACTIVE;

  @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  List<CustomerContact> contacts = new ArrayList<>();

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
