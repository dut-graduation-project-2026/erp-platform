package com.dut.erp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name = "customer_contacts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class CustomerContact {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", nullable = false)
  Customer customer;

  @Column(name = "name", nullable = false)
  String name;

  @Column(name = "email")
  String email;

  @Column(name = "phone", length = 50)
  String phone;

  @Column(name = "position", length = 100)
  String position;

  @Column(name = "is_primary", nullable = false)
  @Builder.Default
  Boolean isPrimary = false;

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
