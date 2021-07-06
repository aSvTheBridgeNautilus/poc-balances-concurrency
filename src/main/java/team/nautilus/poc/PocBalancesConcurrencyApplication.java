package team.nautilus.poc;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import lombok.extern.slf4j.Slf4j;
import team.nautilus.poc.concurrency.persistence.model.Balance;
import team.nautilus.poc.concurrency.persistence.model.constant.OperationType;
import team.nautilus.poc.concurrency.persistence.model.embeddables.BalanceMovement;
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
//				try {
//					balanceRepo.save(Balance
//							.builder()
//							.accountId(111l * i)
//							.amount(0d)
//							.balance(0d)
//							.operationType(OperationType.INITIAL)
//							.timestamp(LocalDateTime
//									.now()
//									.minusMonths(3)
//									.withDayOfMonth(ThreadLocalRandom.current().nextInt(1, 5))
//									.toInstant(ZoneOffset.UTC))
//							.movement(BalanceMovement
//									.builder()
//									.id(0l)
//									.build())
//							.build());
//					
//					balanceRepo.save(Balance
//							.builder()
//							.accountId(111l * i)
//							.amount(1000d * i)
//							.balance(1000d * i)
//							.operationType(OperationType.CREDIT)
//							.timestamp(LocalDateTime
//									.now()
//									.minusMonths(3)
//									.withDayOfMonth(ThreadLocalRandom.current().nextInt(7, 27))
//									.toInstant(ZoneOffset.UTC))
//							.movement(BalanceMovement
//									.builder()
//									.id(1l)
//									.build())
//							.build());
//					
//
//				} catch (Exception e) {
//					log.info("demo data for account {} already inserted", (111l * i));
//				}
//			}
//			
//		};
//	}

}
