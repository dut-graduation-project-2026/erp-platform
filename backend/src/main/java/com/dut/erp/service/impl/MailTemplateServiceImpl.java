package com.dut.erp.service.impl;

import com.dut.erp.config.properties.SystemDomainProperties;
import com.dut.erp.entity.OrganizationInvitation;
import com.dut.erp.service.MailTemplateService;
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
        new StringBuilder(systemDomainProperties.backend())
            .append("/organizations/")
            .append(invitation.getOrganization().getId())
            .append("/invitations/")
            .append(invitation.getId())
            .append("?accept=");

    var context = new Context();
    context.setVariable("organizationName", invitation.getOrganization().getName());
    context.setVariable("inviterName", invitation.getInvitedBy().getEmail());
    context.setVariable("recipientEmail", invitation.getEmail());
    context.setVariable("acceptUrl", invitationLink + "true");
    context.setVariable("declineUrl", invitationLink + "false");
    context.setVariable(
        "expiresAt", invitation.getExpiresAt().atZone(ZoneId.systemDefault()).toLocalDateTime());

    return templateEngine.process("emails/organization-invitation", context);
  }
}
