package com.dut.erp.entity;

import com.dut.erp.enums.ActionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class AuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  UUID id;

  @Column(name = "entity_type", nullable = false, length = 50)
  String entityType;

  @Column(name = "entity_id", nullable = false)
  UUID entityId;

  @Column(name = "action", nullable = false)
  @Enumerated(EnumType.STRING)
  ActionType action;

  @Column(name = "changed_field", length = 100)
  String changedField;

  @Column(name = "old_value", columnDefinition = "TEXT")
  String oldValue;

  @Column(name = "new_value", columnDefinition = "TEXT")
  String newValue;

  @Column(name = "message", nullable = false, columnDefinition = "TEXT")
  String message;

  @Column(name = "organization_id")
  UUID organizationId;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  Instant createdAt;

  @CreatedBy
  @ManyToOne
  @JoinColumn(name = "created_by_id", nullable = false, updatable = false)
  User createdBy;
}
