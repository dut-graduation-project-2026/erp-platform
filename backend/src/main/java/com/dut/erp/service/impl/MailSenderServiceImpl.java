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
import java.io.IOException;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
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
    if ("api.sendgrid.com".equalsIgnoreCase(emailProperties.host()) || emailProperties.port() == 443) {
      return sendViaSendGridApi(request);
    }

    try {
      MimeMessage message = buildMessage(request);
      mailSender.send(message);
      log.info("Email sent successfully via SMTP to {}", request.to());
      return CompletableFuture.completedFuture(null);
    } catch (MessagingException | MailException e) {
      log.error("Failed to send email via SMTP to {}", request.to(), e);
      return CompletableFuture.failedFuture(
          new RuntimeException("Failed to send email via SMTP to " + request.to(), e));
    }
  }

  private CompletableFuture<Void> sendViaSendGridApi(SendMailRequest request) {
    return CompletableFuture.runAsync(() -> {
      try {
        String fromEmail = (emailProperties.fromAddress() != null && !emailProperties.fromAddress().isBlank())
            ? emailProperties.fromAddress()
            : emailProperties.username();

        Email from = new Email(fromEmail);
        String subject = request.subject();
        Email to = new Email(request.to());
        Content content = new Content(request.isHtml() ? "text/html" : "text/plain", request.content());
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(emailProperties.password());
        Request sgRequest = new Request();
        sgRequest.setMethod(Method.POST);
        sgRequest.setEndpoint("mail/send");
        sgRequest.setBody(mail.build());

        Response response = sg.api(sgRequest);
        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
          log.info("Email sent successfully via SendGrid SDK to {}", request.to());
        } else {
          log.error("SendGrid SDK API error (status {}): {}", response.getStatusCode(), response.getBody());
          throw new RuntimeException("SendGrid API returned error status: " + response.getStatusCode());
        }
      } catch (IOException e) {
        log.error("Failed to send email via SendGrid SDK to {}", request.to(), e);
        throw new RuntimeException(e);
      }
    });
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

    String from = (emailProperties.fromAddress() != null && !emailProperties.fromAddress().isBlank())
        ? emailProperties.fromAddress()
        : emailProperties.username();
    helper.setFrom(from);
    helper.setTo(request.to());
    helper.setSubject(request.subject());
    helper.setText(request.content(), request.isHtml());

    return message;
  }
}
