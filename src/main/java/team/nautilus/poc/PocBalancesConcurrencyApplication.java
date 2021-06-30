package team.nautilus.poc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import lombok.extern.slf4j.Slf4j;
import team.nautilus.poc.concurrency.application.facade.BillingPeriodFacade;

@Slf4j
@SpringBootApplication
public class PocBalancesConcurrencyApplication {

	public static void main(String[] args) {
		SpringApplication.run(PocBalancesConcurrencyApplication.class, args);
	}

	@Bean
	public CommandLineRunner startBillingPeriodTaskExecutor(BillingPeriodFacade billingFacade) {
		return args -> {
			while (true) {
				log.info("[PocBalancesConcurrencyApplication:" + "startBillingPeriodTaskExecutor] "
						+ "Billing Period Executor started on {}", Instant.now());
				/*
				 * start Billing Period Task Executor
				 */
				billingFacade.updateBillingPeriods();

				log.info("[PocBalancesConcurrencyApplication:" + "startBillingPeriodTaskExecutor] "
						+ "Billing Period Executor ended on {}", Instant.now());
				
				Thread.sleep(ChronoUnit.NANOS.between(LocalDateTime.now(), LocalDateTime.now().plusMinutes(1)));
			}
		};
	}
}
