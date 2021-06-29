package team.nautilus.poc.concurrency.application.facade.impl;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import team.nautilus.poc.concurrency.application.dto.builder.BalanceBuilder;
import team.nautilus.poc.concurrency.application.dto.request.BalanceInitializationRequest;
import team.nautilus.poc.concurrency.application.dto.response.BalanceResponse;
import team.nautilus.poc.concurrency.application.facade.AccountJournalFacade;
import team.nautilus.poc.concurrency.infrastructure.errors.exceptions.BalanceInitializationException;
import team.nautilus.poc.concurrency.persistence.model.Balance;
import team.nautilus.poc.concurrency.service.AccountJournalBillingPeriod;
import team.nautilus.poc.concurrency.service.BillingPeriodService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountJournalFacadeImpl implements AccountJournalFacade {

	private final AccountJournalBillingPeriod journalService;
	private final BillingPeriodService billingPeriodService;
	
	@Override
	@SneakyThrows
	@Transactional(rollbackFor = RuntimeException.class)
	public BalanceResponse initializeBalance(BalanceInitializationRequest request) {
		try {
			// save initial balance
			
			billingPeriodService.createFirstBillingPeriodForAccount(request);
			log.info("[AccountJournal: InitBalance]: First Billing Period initialized for account{}", request.getAccountId());
			return BalanceResponse
						.builder()
					    .accountId(request.getAccountId())
						.amount(0d)
						.build();
			
		} catch (BalanceInitializationException ex) {
			throw ex;
		} catch (RuntimeException ex) {
			throw new BalanceInitializationException("Initialization request invalid for account " + request.getAccountId());
		}
	}

	@Override
	@SneakyThrows
	public BalanceResponse getBalanceFromCurrentBillingPeriodOfAccount(Long accountId) {
		Balance latestMovement = journalService.getLastMovementFromAccount(accountId);

		return BalanceBuilder.toCurrentBillingPeriodBalanceResponse(
						latestMovement,
						journalService.getCurrentBillingPeriodBalanceByAccountId(accountId));
	}
	
}
