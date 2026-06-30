package com.dut.erp.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@EnableScheduling
public class SchedulingConfig {

  @Bean(name = "taskExecutor")
  public Executor taskExecutor() {
    int availableProcessors = Runtime.getRuntime().availableProcessors();

    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(Math.max(2, Math.min(availableProcessors, 4)));
    executor.setMaxPoolSize(Math.max(4, availableProcessors * 2));
    executor.setQueueCapacity(500);
    executor.setThreadNamePrefix("task-");
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(30);
    executor.initialize();
    return executor;
  }
}
