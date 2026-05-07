package com.dut.erp.service;

import com.dut.erp.entity.OrganizationInvitation;

public interface MailTemplateService {
  String generateOrganizationInvitationEmailContent(OrganizationInvitation invitation);
}
