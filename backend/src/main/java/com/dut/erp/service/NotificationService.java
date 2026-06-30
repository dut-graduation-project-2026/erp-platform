package com.dut.erp.service;

import com.dut.erp.dto.request.PushNotificationRequest;
import com.dut.erp.dto.response.NotificationResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.List;
import java.util.UUID;

public interface NotificationService {
  void pushNotification(PushNotificationRequest request);
  
  List<NotificationResponse> getNotificationsByUserId(UUID userId);
  
  void deleteNotification(UUID id, UUID userId);

  SseEmitter subscribeToUnreadCount(UUID userId);
}
