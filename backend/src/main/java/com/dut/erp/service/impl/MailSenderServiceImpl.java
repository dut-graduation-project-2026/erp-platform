package com.dut.erp.service.impl;

import com.dut.erp.config.properties.EmailProperties;
import com.dut.erp.dto.event.OrganizationInvitationCreatedEvent;
import com.dut.erp.dto.request.SendMailRequest;
import com.dut.erp.entity.OrganizationInvitation;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.repository.OrganizationInvitationRepository;
import com.dut.erp.service.MailSenderService;
import com.dut.erp.service.MailTemplateService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailSenderServiceImpl implements MailSenderService {

  private final JavaMailSender mailSender;
  private final MailTemplateService mailTemplateService;
  private final OrganizationInvitationRepository organizationInvitationRepository;
  private final EmailProperties emailProperties;

  @Async("taskExecutor")
  @Override
  public CompletableFuture<Void> sendMail(SendMailRequest request) {
    try {
      MimeMessage message = buildMessage(request);
      mailSender.send(message);
      log.info("Email sent successfully to {}", request.to());
      return CompletableFuture.completedFuture(null);
    } catch (MessagingException | MailException e) {
      log.error("Failed to send email to {}", request.to(), e);
      return CompletableFuture.failedFuture(
          new RuntimeException("Failed to send email to " + request.to(), e));
    }
  }

  @Async("taskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleInvitationCreated(OrganizationInvitationCreatedEvent event) {

    OrganizationInvitation invitation =
        organizationInvitationRepository
            .findByIdWithContext(event.organizationInvitationId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Invitation not found with id: " + event.organizationInvitationId()));

    String content = mailTemplateService.generateOrganizationInvitationEmailContent(invitation);

    SendMailRequest request =
        new SendMailRequest(
            invitation.getEmail(),
            "You're invited to join " + invitation.getOrganization().getName() + " on ERP Platform",
            content,
            true);

    sendMail(request);

    log.info("Invitation email dispatched for {}", invitation.getEmail());
  }

  private MimeMessage buildMessage(SendMailRequest request) throws MessagingException {

    MimeMessage message = mailSender.createMimeMessage();

    MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

    helper.setFrom(emailProperties.username());
    helper.setTo(request.to());
    helper.setSubject(request.subject());
    helper.setText(request.content(), request.isHtml());

    return message;
  }
}
