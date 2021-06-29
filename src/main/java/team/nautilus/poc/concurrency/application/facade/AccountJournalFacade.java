package team.nautilus.poc.concurrency.application.facade;

import org.springframework.stereotype.Component;

import team.nautilus.poc.concurrency.application.dto.request.BalanceInitializationRequest;
import team.nautilus.poc.concurrency.application.dto.response.BalanceResponse;

@Component
public interface AccountJournalFacade {

	BalanceResponse initializeBalance(BalanceInitializationRequest request);

	BalanceResponse getBalanceFromCurrentBillingPeriodOfAccount(Long accountId);

}
