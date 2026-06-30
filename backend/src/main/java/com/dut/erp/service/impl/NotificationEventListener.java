package com.dut.erp.service.impl;

import com.dut.erp.dto.event.*;
import com.dut.erp.dto.request.PushNotificationRequest;
import com.dut.erp.entity.*;
import com.dut.erp.enums.InvoiceStatus;
import com.dut.erp.enums.OrganizationInvitationStatus;
import com.dut.erp.enums.ReplenishmentStatus;
import com.dut.erp.repository.*;
import com.dut.erp.service.NotificationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

  private final NotificationService notificationService;
  private final OrganizationInvitationRepository organizationInvitationRepository;
  private final UserRepository userRepository;
  private final LeadRepository leadRepository;
  private final OrderRepository orderRepository;
  private final ReplenishmentRequestRepository replenishmentRequestRepository;
  private final InvoiceRepository invoiceRepository;

  @Async
  @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleInvitationCreated(OrganizationInvitationCreatedEvent event) {
    log.info("Processing invitation created event for notification: {}", event.organizationInvitationId());
    OrganizationInvitation invitation = organizationInvitationRepository.findById(event.organizationInvitationId()).orElse(null);
    if (invitation == null) return;
    
    userRepository.findByEmail(invitation.getEmail()).ifPresent(recipient -> {
      String title = "Organization Invitation";
      String message = String.format("You have been invited to join organization %s as %s.",
          invitation.getOrganization().getName(), invitation.getRole().getName());
      notificationService.pushNotification(new PushNotificationRequest(
          List.of(recipient.getId()), title, message, "INFO"));
    });
  }

  @Async
  @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleInvitationStatusChanged(OrganizationInvitationStatusChangedEvent event) {
    log.info("Processing invitation status changed event: {}", event.invitationId());
    OrganizationInvitation invitation = organizationInvitationRepository.findById(event.invitationId()).orElse(null);
    if (invitation == null || invitation.getInvitedBy() == null) return;
    
    String action = event.status() == OrganizationInvitationStatus.ACCEPTED ? "accepted" : "declined";
    String title = "Invitation Response";
    String message = String.format("User %s has %s the invitation to join organization %s.",
        invitation.getEmail(), action, invitation.getOrganization().getName());
    
    notificationService.pushNotification(new PushNotificationRequest(
        List.of(invitation.getInvitedBy().getId()), title, message, "INFO"));
  }

  @Async
  @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleLeadAssigned(LeadAssignedEvent event) {
    log.info("Processing lead assigned event: {}", event.leadId());
    Lead lead = leadRepository.findById(event.leadId()).orElse(null);
    if (lead == null || lead.getSalePerson() == null) return;
    
    String title = "New Lead Assigned";
    String message = String.format("You have been assigned to lead %s.", lead.getName());
    
    notificationService.pushNotification(new PushNotificationRequest(
        List.of(lead.getSalePerson().getId()), title, message, "INFO"));
  }

  @Async
  @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleLeadStageChanged(LeadStageChangedEvent event) {
    log.info("Processing lead stage changed event: {}", event.leadId());
    Lead lead = leadRepository.findById(event.leadId()).orElse(null);
    if (lead == null) return;
    
    User recipient = lead.getSalePerson() != null ? lead.getSalePerson() : lead.getCreatedBy();
    if (recipient == null) return;
    
    String title = "Lead Stage Updated";
    String message = String.format("Lead %s stage has changed to %s.", lead.getName(), event.stage());
    
    notificationService.pushNotification(new PushNotificationRequest(
        List.of(recipient.getId()), title, message, "INFO"));
  }

  @Async
  @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
    log.info("Processing order status changed event: {}", event.orderId());
    Order order = orderRepository.findById(event.orderId()).orElse(null);
    if (order == null || order.getCreatedBy() == null) return;
    
    List<UUID> recipientIds = new ArrayList<>();
    recipientIds.add(order.getCreatedBy().getId());
    
    if (order.getLead() != null && order.getLead().getSalePerson() != null) {
      UUID salesPersonId = order.getLead().getSalePerson().getId();
      if (!recipientIds.contains(salesPersonId)) {
        recipientIds.add(salesPersonId);
      }
    }
    
    String title = "Order Status Updated";
    String actionText = switch (event.newStatus()) {
      case DRAFT -> "is draft";
      case CONFIRMED -> "has been confirmed";
      case WAITING_FOR_STOCK -> "is waiting for stock";
      case SENT -> "has been shipped";
      case COMPLETED -> "has been completed";
      case CANCELLED -> "has been cancelled";
    };
    String message = String.format("Order %s %s.", order.getOrderNumber(), actionText);
    
    notificationService.pushNotification(new PushNotificationRequest(
        recipientIds, title, message, "INFO"));
  }

  @Async
  @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleReplenishmentRequestStatusChanged(ReplenishmentRequestStatusChangedEvent event) {
    log.info("Processing replenishment request event: {}", event.replenishmentId());
    ReplenishmentRequest req = replenishmentRequestRepository.findById(event.replenishmentId()).orElse(null);
    if (req == null) return;
    
    if (event.oldStatus() == null) {
      List<UUID> recipientIds = new ArrayList<>();
      if (req.getWarehouse().getManager() != null) {
        recipientIds.add(req.getWarehouse().getManager().getId());
      }
      if (req.getWarehouse().getStaff() != null) {
        for (User staff : req.getWarehouse().getStaff()) {
          if (!recipientIds.contains(staff.getId())) {
            recipientIds.add(staff.getId());
          }
        }
      }
      if (req.getCreatedBy() != null && !recipientIds.contains(req.getCreatedBy().getId())) {
        recipientIds.add(req.getCreatedBy().getId());
      }
      
      if (recipientIds.isEmpty()) return;
      
      String title = "New Replenishment Request";
      String message = String.format("A new replenishment request has been created for warehouse %s.",
          req.getWarehouse().getName());
      
      notificationService.pushNotification(new PushNotificationRequest(
          recipientIds, title, message, "INFO"));
    } else if (event.newStatus() == ReplenishmentStatus.RESOLVED) {
      if (req.getCreatedBy() == null) return;
      
      String title = "Replenishment Request Resolved";
      String message = String.format("Replenishment request for document %s at warehouse %s has been resolved.",
          req.getInventoryDocument().getName(), req.getWarehouse().getName());
      
      notificationService.pushNotification(new PushNotificationRequest(
          List.of(req.getCreatedBy().getId()), title, message, "SUCCESS"));
    }
  }

  @Async
  @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleInvoiceStatusChanged(InvoiceStatusChangedEvent event) {
    log.info("Processing invoice status changed event: {}", event.invoiceId());
    Invoice invoice = invoiceRepository.findById(event.invoiceId()).orElse(null);
    if (invoice == null) return;
    
    List<UUID> recipientIds = new ArrayList<>();
    if (invoice.getCreatedBy() != null) {
      recipientIds.add(invoice.getCreatedBy().getId());
    }
    if (invoice.getOrder() != null && invoice.getOrder().getCreatedBy() != null) {
      UUID orderCreatorId = invoice.getOrder().getCreatedBy().getId();
      if (!recipientIds.contains(orderCreatorId)) {
        recipientIds.add(orderCreatorId);
      }
    }
    
    if (recipientIds.isEmpty()) return;
    
    String title = "";
    String message = "";
    String type = "INFO";
    
    if (event.oldStatus() == null) {
      title = "New Invoice Created";
      message = String.format("Invoice %s has been created for order %s.",
          invoice.getInvoiceNumber(), invoice.getOrder() != null ? invoice.getOrder().getOrderNumber() : "");
    } else if (event.newStatus() == InvoiceStatus.PAID) {
      title = "Invoice Paid Successfully";
      message = String.format("Invoice %s has been successfully paid.", invoice.getInvoiceNumber());
      type = "SUCCESS";
    } else {
      title = "Invoice Status Updated";
      message = String.format("Invoice %s status changed to %s.", invoice.getInvoiceNumber(), event.newStatus());
    }
    
    notificationService.pushNotification(new PushNotificationRequest(
        recipientIds, title, message, type));
  }
}
