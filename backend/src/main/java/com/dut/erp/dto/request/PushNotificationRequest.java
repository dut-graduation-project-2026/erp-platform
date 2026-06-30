package com.dut.erp.dto.request;

import com.dut.erp.annotation.ValueOfEnum;
import com.dut.erp.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.UUID;

public record PushNotificationRequest(
    @NotEmpty(message = "Recipient IDs cannot be empty") List<UUID> recipientIds,
    @NotBlank(message = "Title cannot be blank") String title,
    String message,
    @Pattern(regexp = "^\\S+$", message = "Notification type must not contain whitespace")
        @NotNull(message = "Notification type cannot be null")
        @ValueOfEnum(
            enumClass = NotificationType.class,
            message = "Notification type must be one of: {enumValues}")
        String notificationType) {}
