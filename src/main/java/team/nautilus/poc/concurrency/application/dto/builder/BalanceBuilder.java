package team.nautilus.poc.concurrency.application.dto.builder;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

import lombok.extern.slf4j.Slf4j;
import team.nautilus.poc.concurrency.application.dto.request.BalanceInitializationRequest;
import team.nautilus.poc.concurrency.application.dto.response.BalanceResponse;
import team.nautilus.poc.concurrency.persistence.model.Balance;
import team.nautilus.poc.concurrency.persistence.model.BillingPeriod;

@Slf4j
public class BalanceBuilder {

	public static BalanceResponse toResponse(Balance balance) {
		return BalanceResponse
				.builder()
				.id(balance.getId())
				.accountId(balance.getAccountId())
				.amount(balance.getAmount())
				.timestamp(balance.getTimestamp())
				.build();
	}

	public static BalanceResponse toCurrentBalanceResponse(Balance balance) {
		return BalanceResponse
				.builder()
				.id(balance.getId())
				.accountId(balance.getAccountId())
				.amount(balance.getBalance())
				.timestamp(balance.getTimestamp())
				.build();
	}

	public static BalanceResponse toCurrentBillingPeriodBalanceResponse(Balance balance, Double billingPeriodBalance) {
		return BalanceResponse
				.builder()
				.id(balance.getId())
				.accountId(balance.getAccountId())
				.amount(billingPeriodBalance)
				.timestamp(balance.getTimestamp())
				.build();
	}

	public static BillingPeriod toInitialBillingPeriod(BalanceInitializationRequest request) {
		return BillingPeriod
				.builder()
				.accountId(request.getAccountId())
				.userId(request.getUserId())
				.billingDate(LocalDate.now())
//				.billingDay(Math.min(LocalDate.now().getDayOfMonth(), 27))
				.billingDay(ThreadLocalRandom.current().nextInt(1, 17))
				.billingCycle(30)
				.balance(0d)
				.build();
	}

}
