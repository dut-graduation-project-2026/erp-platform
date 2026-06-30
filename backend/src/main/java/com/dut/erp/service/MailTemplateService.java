package com.dut.erp.service;

import com.dut.erp.entity.OrganizationInvitation;
import java.time.Instant;

public interface MailTemplateService {
  String generateOrganizationInvitationEmailContent(OrganizationInvitation invitation);

  String generateForgotPasswordEmailContent(String email, String token, Instant expiresAt);
}
