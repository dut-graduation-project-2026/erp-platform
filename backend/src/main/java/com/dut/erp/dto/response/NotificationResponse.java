package com.dut.erp.dto.response;

import com.dut.erp.entity.Notification;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationResponse {
  UUID id;
  String title;
  String message;
  String type;
  Boolean isRead;
  Instant createdAt;

  public static NotificationResponse fromEntity(Notification entity) {
    if (entity == null) {
      return null;
    }
    return NotificationResponse.builder()
        .id(entity.getId())
        .title(entity.getTitle())
        .message(entity.getMessage())
        .type(entity.getType())
        .isRead(entity.getIsRead())
        .createdAt(entity.getCreatedAt())
        .build();
  }
}
