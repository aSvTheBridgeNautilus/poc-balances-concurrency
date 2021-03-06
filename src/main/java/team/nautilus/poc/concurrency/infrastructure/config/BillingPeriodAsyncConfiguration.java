package team.nautilus.poc.concurrency.infrastructure.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableAsync
public class BillingPeriodAsyncConfiguration {
	
	public static final String BILLING_PERIOD_TASK_EXECUTOR = "billingPeriodTaskExecutor";

	@Bean(name = "billingPeriodTaskExecutor")
	public Executor taskExecutor() {
		log.debug("[BillingPeriodAsyncConfiguration:taskExecutor] Creating async task executor to manage Billing Periods");
		final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(2);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("BillingPeriodThread-");
		executor.initialize();
		return executor;
	}

}