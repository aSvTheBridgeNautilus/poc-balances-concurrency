package team.nautilus.poc.concurrency.service;

import team.nautilus.poc.concurrency.application.dto.request.BalanceCreditRequest;
import team.nautilus.poc.concurrency.application.dto.request.BalanceDebitRequest;
import team.nautilus.poc.concurrency.application.dto.response.BalanceResponse;
import team.nautilus.poc.concurrency.persistence.model.Balance;

public interface AccountJournalBillingPeriod {

	BalanceResponse takeFundsFromAccount(BalanceDebitRequest request);

	BalanceResponse addFundsToAccount(BalanceCreditRequest request);
}
