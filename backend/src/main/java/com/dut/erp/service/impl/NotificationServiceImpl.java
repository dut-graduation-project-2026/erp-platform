package com.dut.erp.service.impl;

import com.dut.erp.dto.request.PushNotificationRequest;
import com.dut.erp.dto.response.NotificationResponse;
import com.dut.erp.entity.Notification;
import com.dut.erp.entity.User;
import com.dut.erp.exception.ForbiddenException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.repository.NotificationRepository;
import com.dut.erp.repository.UserRepository;
import com.dut.erp.service.NotificationService;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationServiceImpl implements NotificationService {

  NotificationRepository notificationRepository;
  UserRepository userRepository;
  Map<UUID, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

  @Override
  @Transactional
  public void pushNotification(PushNotificationRequest request) {
    if (request.recipientIds() == null || request.recipientIds().isEmpty()) {
      return;
    }

    for (UUID userId : request.recipientIds()) {
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

      Notification notification = Notification.builder()
          .recipient(user)
          .title(request.title())
          .message(request.message())
          .type(request.notificationType())
          .isRead(false)
          .build();

      notificationRepository.save(notification);
      broadcastUnreadCount(userId);
    }
  }

  @Override
  @Transactional
  public List<NotificationResponse> getNotificationsByUserId(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    List<Notification> notifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
    
    boolean updated = false;
    for (Notification notification : notifications) {
      if (!notification.getIsRead()) {
        notification.setIsRead(true);
        updated = true;
      }
    }

    if (updated) {
      notificationRepository.saveAll(notifications);
      broadcastUnreadCount(userId);
    }

    return notifications.stream()
        .map(NotificationResponse::fromEntity)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void deleteNotification(UUID id, UUID userId) {
    Notification notification = notificationRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        
    if (!notification.getRecipient().getId().equals(userId)) {
      throw new ForbiddenException("You are not authorized to delete this notification");
    }

    boolean wasUnread = !notification.getIsRead();
    notificationRepository.delete(notification);

    if (wasUnread) {
      broadcastUnreadCount(userId);
    }
  }

  @Override
  public SseEmitter subscribeToUnreadCount(UUID userId) {
    // 30 minutes timeout
    SseEmitter emitter = new SseEmitter(1800000L);
    
    List<SseEmitter> userEmitters = emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>());
    userEmitters.add(emitter);

    emitter.onCompletion(() -> removeEmitter(userId, emitter));
    emitter.onTimeout(() -> removeEmitter(userId, emitter));
    emitter.onError((ex) -> removeEmitter(userId, emitter));

    // Send the initial unread count in a try-catch to handle client immediate disconnection
    try {
      long unreadCount = notificationRepository.countByRecipientIdAndIsRead(userId, false);
      emitter.send(SseEmitter.event()
          .name("unread-count")
          .data(unreadCount));
    } catch (IOException e) {
      log.error("Failed to send initial unread count for user: {}", userId, e);
      removeEmitter(userId, emitter);
      emitter.completeWithError(e);
    }

    return emitter;
  }

  private void removeEmitter(UUID userId, SseEmitter emitter) {
    List<SseEmitter> userEmitters = emitters.get(userId);
    if (userEmitters != null) {
      userEmitters.remove(emitter);
      if (userEmitters.isEmpty()) {
        emitters.remove(userId);
      }
    }
  }

  private void broadcastUnreadCount(UUID userId) {
    List<SseEmitter> userEmitters = emitters.get(userId);
    if (userEmitters == null || userEmitters.isEmpty()) {
      return;
    }

    long unreadCount = notificationRepository.countByRecipientIdAndIsRead(userId, false);
    List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

    for (SseEmitter emitter : userEmitters) {
      try {
        emitter.send(SseEmitter.event()
            .name("unread-count")
            .data(unreadCount));
      } catch (Exception e) {
        log.warn("Failed to broadcast to emitter for user: {}, removing emitter", userId, e);
        deadEmitters.add(emitter);
      }
    }

    if (!deadEmitters.isEmpty()) {
      userEmitters.removeAll(deadEmitters);
      if (userEmitters.isEmpty()) {
        emitters.remove(userId);
      }
    }
  }
}
