package com.dut.erp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(
    name = "invalidated_access_tokens",
    indexes = {
      @Index(name = "idx_invalidated_access_tokens_expires_at", columnList = "expires_at")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvalidatedAccessToken {

  @Id
  @Column(updatable = false, length = 255)
  String jti;

  @Column(name = "expires_at", nullable = false)
  Instant expiresAt;
}
