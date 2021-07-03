package team.nautilus.poc;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import lombok.extern.slf4j.Slf4j;
import team.nautilus.poc.concurrency.application.facade.BillingPeriodFacade;
import team.nautilus.poc.concurrency.persistence.model.Balance;
import team.nautilus.poc.concurrency.persistence.model.BillingPeriod;
import team.nautilus.poc.concurrency.persistence.repository.BalanceRepository;
import team.nautilus.poc.concurrency.persistence.repository.BillingPeriodRepository;

@Slf4j
@SpringBootApplication
public class PocBalancesConcurrencyApplication {

	public static void main(String[] args) {
		SpringApplication.run(PocBalancesConcurrencyApplication.class, args);
	}

//	@Bean
//	public CommandLineRunner demoData(BalanceRepository balanceRepo, BillingPeriodRepository billingRepo) {		
//		return args -> {
//			
//			for(int i = 1; i <= 5; i++) {
//				Balance newMov = balanceRepo.save(Balance
//					    .builder()
//					    .accountId(111l * i)
//					    .amount(0d)
//					    .balance(0d)
//					    .timestamp(LocalDateTime
//					    		.now()
//					    		.minusMonths(3)
//					    		.withDayOfMonth(ThreadLocalRandom.current().nextInt(1, 27))
//					    		.toInstant(ZoneOffset.UTC))
//						.build());
//				
//				billingRepo.save(BillingPeriod
//						.builder()
//						.accountId(newMov.getAccountId())
//						.userId("user" + (111l * i) + "@nautilus.team")
//						.timestamp(Instant.parse(
//								LocalDate
//					    		.now()
//					    		.atStartOfDay()
//					    		.minusMonths(3)
//					    		.withDayOfMonth(1)
//					    		.toString() + ":00Z"))
//						.billingDay(ThreadLocalRandom.current().nextInt(1, 17))
//						.billingCycle(30)
//						.transactionsCycle(100l)
//						.count(0l)
//						.balance(newMov.getAmount())
//						.build());
//			}
//			
//		};
//	}
	
//	@Bean
//	public CommandLineRunner startBillingPeriodTaskExecutor(BillingPeriodFacade billingFacade) {
//		return args -> {
//			while (true) {
//				Thread.sleep(ChronoUnit.NANOS.between(LocalDateTime.now(), LocalDateTime.now().plusMinutes(1)));
//				log.info("[PocBalancesConcurrencyApplication:" + "startBillingPeriodTaskExecutor] "
//						+ "Billing Period Executor started on {}", Instant.now());
//				/*
//				 * start Billing Period Task Executor
//				 */
//				billingFacade.updateBillingPeriods();
//
//				log.info("[PocBalancesConcurrencyApplication:" + "startBillingPeriodTaskExecutor] "
//						+ "Billing Period Executor ended on {}", Instant.now());
//				
//			}
//		};
//	}
}
