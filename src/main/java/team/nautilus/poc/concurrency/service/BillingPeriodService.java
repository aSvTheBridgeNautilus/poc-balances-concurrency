package team.nautilus.poc.concurrency.service;

import static team.nautilus.poc.concurrency.infrastructure.config.BillingPeriodAsyncConfiguration.BILLING_PERIOD_TASK_EXECUTOR;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;

import team.nautilus.poc.concurrency.application.dto.request.BalanceInitializationRequest;
import team.nautilus.poc.concurrency.persistence.model.BillingPeriod;

public interface BillingPeriodService {

	LocalDate getCurrentBillingPeriodDate(Long accountId);

	List<BillingPeriod> getAllBillingPeriodsForUpdate();

	BillingPeriod getCurrentBillingPeriodFromAccount(Long accountId);

	default void processNewBillingCycle(BillingPeriod currentPeriod, Double currentBalance) {
		processNewBillingCycle(currentPeriod, currentBalance, false);
	}

	void processNewBillingCycle(BillingPeriod currentPeriod, Double currentBalance, boolean isTransactionCycle);

	void createFirstBillingPeriodForAccount(BalanceInitializationRequest request);
	
	@Async(BILLING_PERIOD_TASK_EXECUTOR)
	CompletableFuture<Double> getCurrenBillingPeriodBalanceFromAccount(Long accountId);

}
