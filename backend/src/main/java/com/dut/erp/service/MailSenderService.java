package com.dut.erp.service;

import com.dut.erp.dto.request.SendMailRequest;
import java.util.concurrent.CompletableFuture;

public interface MailSenderService {
  CompletableFuture<Void> sendMail(SendMailRequest request);
}
