package team.nautilus.poc.concurrency.application.facade;

import javax.validation.Valid;

import org.springframework.stereotype.Component;

import team.nautilus.poc.concurrency.application.dto.request.BalanceCreditRequest;
import team.nautilus.poc.concurrency.application.dto.request.BalanceDebitRequest;
import team.nautilus.poc.concurrency.application.dto.request.BalanceInitializationRequest;
import team.nautilus.poc.concurrency.application.dto.request.BalanceTransferRequest;
import team.nautilus.poc.concurrency.application.dto.response.BalanceResponse;
import team.nautilus.poc.concurrency.application.dto.response.TransferResponse;
import team.nautilus.poc.concurrency.persistence.model.Balance;

@Component
public interface AccountJournalFacade {

	BalanceResponse initializeBalance(BalanceInitializationRequest request);

	BalanceResponse getBalanceFromCurrentBillingPeriodOfAccount(Long accountId);

	boolean verifyAccountHasSufficientFunds(Balance lastMovement, Double amountRequired);

	BalanceResponse addFundsToAccount(@Valid BalanceCreditRequest creditRequest);

	BalanceResponse takeFundsFromAccount(@Valid BalanceDebitRequest debitRequest);

	TransferResponse registerTransfer(@Valid BalanceTransferRequest transferRequest);

}
