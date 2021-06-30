package team.nautilus.poc.concurrency.service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import team.nautilus.poc.concurrency.application.dto.request.BalanceInitializationRequest;
import team.nautilus.poc.concurrency.persistence.model.BillingPeriod;

public interface BillingPeriodService {

	LocalDate getCurrentBillingPeriodDate(Long accountId);

	List<BillingPeriod> getAllBillingPeriodsForUpdate();

	void processNewBillingCycle(BillingPeriod currentPeriod, Double currentBalance);

	void createFirstBillingPeriodForAccount(BalanceInitializationRequest request);
	
	CompletableFuture<Double> getCurrenBillingPeriodBalanceFromAccount(Long accountId);

}
