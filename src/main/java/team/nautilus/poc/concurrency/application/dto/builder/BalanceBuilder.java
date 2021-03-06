package team.nautilus.poc.concurrency.application.dto.builder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

import lombok.extern.slf4j.Slf4j;
import team.nautilus.poc.concurrency.application.dto.request.BalanceCreditRequest;
import team.nautilus.poc.concurrency.application.dto.request.BalanceDebitRequest;
import team.nautilus.poc.concurrency.application.dto.request.BalanceInitializationRequest;
import team.nautilus.poc.concurrency.application.dto.response.BalanceResponse;
import team.nautilus.poc.concurrency.persistence.model.Balance;
import team.nautilus.poc.concurrency.persistence.model.BillingPeriod;
import team.nautilus.poc.concurrency.persistence.model.constant.OperationType;

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
				.timestamp(Instant.parse(LocalDate.now().atStartOfDay().toString() + ":00Z"))
//				.billingDay(Math.min(LocalDate.now().getDayOfMonth(), 27))
				.billingDay(ThreadLocalRandom.current().nextInt(1, 17))
				.billingCycle(30)
				.balance(0d)
				.build();
	}
	
	public static Balance toNewTopDebitMovement(
			Balance source, 
			BalanceDebitRequest request, 
			Instant timestamp) {
		
		Double balance = source.getBalance() - request.getAmount();
		Double amount = -request.getAmount();
		
		Balance newMovement = Balance
				.builder()
				.accountId(source.getAccountId())
				.balance(balance)
				.amount(amount)
				.timestamp(timestamp)
				.build();
		
		return newMovement;
	}
	
	public static Balance toNewTopCreditMovement(
			Balance source, 
			BalanceCreditRequest request, 
			Instant timestamp) {
		
		Double balance = source.getBalance() + request.getAmount();
		Double amount = request.getAmount();
		
		Balance newMovement = Balance
				.builder()
				.accountId(source.getAccountId())
				.balance(balance)
				.amount(amount)
				.timestamp(timestamp)
				.build();
		
		return newMovement;
	}
	


}
