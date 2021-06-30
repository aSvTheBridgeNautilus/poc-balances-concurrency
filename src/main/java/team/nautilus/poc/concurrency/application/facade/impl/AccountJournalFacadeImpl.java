package team.nautilus.poc.concurrency.application.facade.impl;

import java.time.Instant;
import java.time.LocalDate;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import team.nautilus.poc.concurrency.application.dto.builder.BalanceBuilder;
import team.nautilus.poc.concurrency.application.dto.request.BalanceInitializationRequest;
import team.nautilus.poc.concurrency.application.dto.response.BalanceResponse;
import team.nautilus.poc.concurrency.application.facade.AccountJournalFacade;
import team.nautilus.poc.concurrency.application.mapper.dto.LocalDate2InstantUTCMapper;
import team.nautilus.poc.concurrency.infrastructure.errors.exceptions.BalanceInitializationException;
import team.nautilus.poc.concurrency.persistence.model.Balance;
import team.nautilus.poc.concurrency.persistence.repository.BalanceRepository;
import team.nautilus.poc.concurrency.service.AccountJournalBillingPeriod;
import team.nautilus.poc.concurrency.service.BillingPeriodService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountJournalFacadeImpl implements AccountJournalFacade {

	private final AccountJournalBillingPeriod journalService;
	private final BillingPeriodService billingService;
	private final BalanceRepository balanceRepository;
	private final LocalDate2InstantUTCMapper dateUTCMapper;
	
	@Override
	@SneakyThrows
	@Transactional(rollbackFor = RuntimeException.class)
	public BalanceResponse initializeBalance(BalanceInitializationRequest request) {
		try {
			// save initial balance
			balanceRepository.save(Balance
					    .builder()
					    .accountId(request.getAccountId())
					    .amount(0d)
					    .balance(0d)
					    .timestamp(Instant.now())
						.build());
			/*
			 * implement from here down
			 */
			billingService.createFirstBillingPeriodForAccount(request);
			log.info("[AccountJournal:initializeBalance]: First billing period of account {} initialized", request.getAccountId());
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
	
	@SneakyThrows
	public Double getCurrentBillingPeriodBalanceByAccountId(Long id) {
		return repository.getCurrentBillingPeriodBalanceByAccountId(
				id,
				dateUTCMapper.toDTO(billingService.getCurrentBillingPeriodDate(id)));
	}

	@Override
	@SneakyThrows
	public BalanceResponse getBalanceFromCurrentBillingPeriodOfAccount(Long accountId) {
		log.info("[AccountJournal:getBalanceFromCurrentBillingPeriodOfAccount]: Calculating balance of current Billing Period for account{}", accountId);
		
		// get las movement form account
		Balance latestMovement = journalService.getLastMovementFromAccount(accountId);
		
		// get last billing date of account
		Instant latestBillingdate = dateUTCMapper.toDTO(billingService.getCurrentBillingPeriodDate(accountId));

		return BalanceBuilder.toCurrentBillingPeriodBalanceResponse(
						latestMovement,
						journalService.getCurrentBillingPeriodBalanceByAccountId(accountId));
	}
	
}
