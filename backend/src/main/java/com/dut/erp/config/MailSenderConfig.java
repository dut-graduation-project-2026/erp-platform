package com.dut.erp.config;

import com.dut.erp.config.properties.EmailProperties;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(EmailProperties.class)
public class MailSenderConfig {
  private static final String SMTP_TRANSPORT_PROTOCOL = "smtp";
  private static final String DEFAULT_TIMEOUT_MS = "5000";

  private final EmailProperties props;

  @Bean
  public JavaMailSender javaMailSender() {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost(props.host());
    mailSender.setPort(parsePort(props.port()));
    mailSender.setUsername(props.username());
    mailSender.setPassword(props.password());

    configureTransportProperties(mailSender.getJavaMailProperties());

    log.info("JavaMailSender configured successfully with host: {}", props.host());
    return mailSender;
  }

  private int parsePort(String rawPort) {
    try {
      return Integer.parseInt(rawPort);
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException("Invalid SMTP port: " + rawPort, ex);
    }
  }

  private void configureTransportProperties(Properties properties) {
    properties.put("mail.transport.protocol", SMTP_TRANSPORT_PROTOCOL);
    properties.put("mail.smtp.auth", "true");
    properties.put("mail.smtp.starttls.enable", "true");
    properties.put("mail.smtp.starttls.required", "true");
    properties.put("mail.smtp.connectiontimeout", DEFAULT_TIMEOUT_MS);
    properties.put("mail.smtp.timeout", DEFAULT_TIMEOUT_MS);
    properties.put("mail.smtp.writetimeout", DEFAULT_TIMEOUT_MS);
  }
}
