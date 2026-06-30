package com.dut.erp.controller.v1;

import com.dut.erp.dto.response.NotificationResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping
  public ResponseEntity<List<NotificationResponse>> getMyNotifications(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    List<NotificationResponse> responses = notificationService.getNotificationsByUserId(userDetails.getId());
    return ResponseEntity.ok(responses);
  }

  @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter streamUnreadCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
    log.info("SSE subscription request received for user: {}", userDetails.getId());
    return notificationService.subscribeToUnreadCount(userDetails.getId());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteNotification(
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    notificationService.deleteNotification(id, userDetails.getId());
    return ResponseEntity.noContent().build();
  }
}
