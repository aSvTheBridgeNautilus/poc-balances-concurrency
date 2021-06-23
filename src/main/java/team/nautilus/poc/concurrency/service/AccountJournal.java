package team.nautilus.poc.concurrency.service;

import team.nautilus.poc.concurrency.application.dto.request.BalanceCreditRequest;
import team.nautilus.poc.concurrency.application.dto.request.BalanceDebitRequest;
import team.nautilus.poc.concurrency.application.dto.response.BalanceResponse;
import team.nautilus.poc.concurrency.persistence.repository.model.Balance;

public interface AccountJournal {

	BalanceResponse takeFundsFromAccount(BalanceDebitRequest request);

	BalanceResponse addFundsToAccount(BalanceCreditRequest request);

}
