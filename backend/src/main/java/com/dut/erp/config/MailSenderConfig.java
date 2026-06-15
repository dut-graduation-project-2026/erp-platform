package com.dut.erp.config;

import com.dut.erp.config.properties.EmailProperties;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MailSenderConfig {
  private static final String SMTP_TRANSPORT_PROTOCOL = "smtp";
  private static final String DEFAULT_TIMEOUT_MS = "5000";
  private static final String DEFAULT_SMTP_POOL_SIZE = "10";

  private final EmailProperties props;

  @Bean
  public JavaMailSender javaMailSender() {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost(props.host());
    mailSender.setPort(props.port());
    mailSender.setUsername(props.username());
    mailSender.setPassword(props.password());

    configureTransportProperties(mailSender.getJavaMailProperties());
    log.info("JavaMailSender configured successfully with host: {}", props.host());
    return mailSender;
  }

  private void configureTransportProperties(Properties properties) {
    properties.put("mail.transport.protocol", SMTP_TRANSPORT_PROTOCOL);
    properties.put("mail.smtp.auth", "true");
    properties.put("mail.smtp.starttls.enable", "true");
    properties.put("mail.smtp.starttls.required", "true");
    properties.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
    properties.put("mail.smtp.connectiontimeout", DEFAULT_TIMEOUT_MS);
    properties.put("mail.smtp.timeout", DEFAULT_TIMEOUT_MS);
    properties.put("mail.smtp.writetimeout", DEFAULT_TIMEOUT_MS);
    properties.put("mail.smtp.pool", "true");
    properties.put("mail.smtp.pool.size", DEFAULT_SMTP_POOL_SIZE);
    properties.put("mail.smtp.quitwait", "false");
  }
}
