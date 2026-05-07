package com.dut.erp.service;

import com.dut.erp.dto.request.SendMailRequest;

public interface MailSenderService {
  void sendMail(SendMailRequest request);
}
