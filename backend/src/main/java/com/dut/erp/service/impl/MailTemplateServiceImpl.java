package com.dut.erp.service.impl;

import com.dut.erp.config.properties.SystemDomainProperties;
import com.dut.erp.entity.OrganizationInvitation;
import com.dut.erp.service.MailTemplateService;
import java.time.Instant;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class MailTemplateServiceImpl implements MailTemplateService {
  private final SpringTemplateEngine templateEngine;
  private final SystemDomainProperties systemDomainProperties;

  @Override
  public String generateOrganizationInvitationEmailContent(OrganizationInvitation invitation) {
    StringBuilder invitationLink =
        new StringBuilder(systemDomainProperties.frontend())
            .append("/organizations/")
            .append(invitation.getOrganization().getId())
            .append("/invitations/")
            .append(invitation.getId())
            .append("?accept=");

    StringBuilder inviterName = new StringBuilder();
    if (invitation.getInvitedBy().getFirstName() != null
        && invitation.getInvitedBy().getLastName() != null
        && !invitation.getInvitedBy().getFirstName().isEmpty()
        && !invitation.getInvitedBy().getLastName().isEmpty()) {
      inviterName.append(invitation.getInvitedBy().getFirstName());
      inviterName.append(" ");
      inviterName.append(invitation.getInvitedBy().getLastName());
    } else {
      inviterName.append(invitation.getInvitedBy().getEmail());
    }

    String emailParam = "&email=" + java.net.URLEncoder.encode(invitation.getEmail(), java.nio.charset.StandardCharsets.UTF_8)
        + "&orgName=" + java.net.URLEncoder.encode(invitation.getOrganization().getName(), java.nio.charset.StandardCharsets.UTF_8);

    var context = new Context();
    context.setVariable("organizationName", invitation.getOrganization().getName());
    context.setVariable("inviterName", inviterName.toString());
    context.setVariable("recipientEmail", invitation.getEmail());
    context.setVariable("acceptUrl", invitationLink + "true" + emailParam);
    context.setVariable("declineUrl", invitationLink + "false" + emailParam);
    context.setVariable(
        "expiresAt", invitation.getExpiresAt().atZone(ZoneId.systemDefault()).toLocalDateTime());

    return templateEngine.process("emails/organization-invitation", context);
  }

  @Override
  public String generateForgotPasswordEmailContent(String email, String token, Instant expiresAt) {
    String resetUrl = systemDomainProperties.frontend() + "/reset-password?token=" + token;

    var context = new Context();
    context.setVariable("recipientEmail", email);
    context.setVariable("resetUrl", resetUrl);
    context.setVariable(
        "expiresAt", expiresAt.atZone(ZoneId.systemDefault()).toLocalDateTime());

    return templateEngine.process("emails/forgot-password", context);
  }
}
