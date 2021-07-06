package team.nautilus.poc.concurrency.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import team.nautilus.poc.concurrency.application.dto.BillingPeriodTransactionData;
import team.nautilus.poc.concurrency.application.dto.request.BalanceInitializationRequest;
import team.nautilus.poc.concurrency.persistence.model.Balance;
import team.nautilus.poc.concurrency.persistence.model.BillingPeriod;

public interface BillingPeriodService {

	LocalDate getCurrentBillingPeriodDate(Long accountId);

	BillingPeriod getCurrentBillingPeriodFromAccount(Long accountId);

	void createFirstBillingPeriodForAccount(BalanceInitializationRequest request);

	Double getCurrenBillingPeriodBalanceFromAccount(Balance lastMovementOfPeriod);

	void processNewBillingCycle(Balance lastMovementOfPeriod, Long transactionCycle, Double currentBalance);

	BillingPeriodTransactionData getBillingPeriodTransactionsData(Long accountId, Long lastMovementIdOfPeriod,
			Instant lastMovementTimestampOfPeriod);

}
