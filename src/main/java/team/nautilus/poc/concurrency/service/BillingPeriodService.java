package team.nautilus.poc.concurrency.service;

import java.time.Instant;

import org.springframework.transaction.annotation.Transactional;

import team.nautilus.poc.concurrency.application.dto.BillingPeriodTransactionData;
import team.nautilus.poc.concurrency.application.dto.request.BalanceInitializationRequest;
import team.nautilus.poc.concurrency.persistence.model.Balance;
import team.nautilus.poc.concurrency.persistence.model.BillingPeriod;

public interface BillingPeriodService {

//	BillingPeriod getCurrentBillingPeriodFromAccount(Long accountId);

	void createFirstBillingPeriodForAccount(BalanceInitializationRequest request);

//	Double getCurrenBillingPeriodBalanceFromAccount(Balance lastMovementOfPeriod);

	BillingPeriod processNewBillingCycle(Balance lastMovementOfPeriod, BillingPeriod lastBillingPeriod, Double currentBalance);

	BillingPeriodTransactionData getBillingPeriodTransactionsData(Long accountId, Long lastMovementIdOfPeriod,
			Instant lastMovementTimestampOfPeriod);

	BillingPeriod getLastBillingPeriodFromAccountIfNotFoundCreateInitial(Long accountId);

	BillingPeriod getCurrentBillingPeriodFromAccount(Balance lastMovementOfPeriod);

	Long getLatestMovementIdFromAccountBillingPeriods(Long accountId);

	void verifyTransactionCycleIsExhaustedFor(BillingPeriod period);

	Long getTotalTransactionsFromBillingPeriod(BillingPeriod period);

	Long getTotalTransactionsFromCurrentBillingPeriod(Long accountId);

}
