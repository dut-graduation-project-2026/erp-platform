package com.dut.erp.repository;

import com.dut.erp.entity.Notification;
import com.dut.erp.entity.User;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
  List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);

  long countByRecipientIdAndIsRead(UUID recipientId, boolean isRead);
}
