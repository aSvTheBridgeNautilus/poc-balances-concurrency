package team.nautilus.poc.concurrency.service.impl;

import java.time.Instant;
import java.util.ConcurrentModificationException;
import java.util.List;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import team.nautilus.poc.concurrency.application.dto.BillingPeriodTransactionData;
import team.nautilus.poc.concurrency.application.dto.builder.BalanceBuilder;
import team.nautilus.poc.concurrency.application.dto.request.BalanceInitializationRequest;
import team.nautilus.poc.concurrency.application.mapper.dto.BillingPeriodTransactionDataMapper;
import team.nautilus.poc.concurrency.infrastructure.errors.exceptions.ProcessNewBillingCycleException;
import team.nautilus.poc.concurrency.persistence.model.Balance;
import team.nautilus.poc.concurrency.persistence.model.BillingPeriod;
import team.nautilus.poc.concurrency.persistence.repository.BalanceRepository;
import team.nautilus.poc.concurrency.persistence.repository.BillingPeriodRepository;
import team.nautilus.poc.concurrency.service.BillingPeriodService;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingPeriodServiceImpl implements BillingPeriodService {

	private final BillingPeriodRepository billingRepository;
	private final BalanceRepository balanceRepository;
	private final BillingPeriodTransactionDataMapper transactionDataMapper;
	
	@Override
	@SneakyThrows
	@Transactional(rollbackFor = RuntimeException.class)
	public void processNewBillingCycle(Balance lastMovementOfPeriod, Long transactionCycle, Double currentBalance) {
		log.debug("[BillingPeriodServiceImpl:processNewBillingCycle] Started for account {}",
				lastMovementOfPeriod.getAccountId());
		try {
			BillingPeriod newPeriod = BillingPeriod
					.builder()
					.accountId(lastMovementOfPeriod.getAccountId())
					.userId("user" + lastMovementOfPeriod.getAccountId() + "@nautilus.team")
					.timestamp(lastMovementOfPeriod.getTimestamp()) 
					.movementId(lastMovementOfPeriod.getMovement().getId())
					.transactionsCycle(transactionCycle)
					.balance(currentBalance)
					.build();

			billingRepository.save(newPeriod);
		} catch (ConstraintViolationException e) {
			log.error("[BillingPeriodServiceImpl:processNewBillingCycle] "
					+ "Billing cycle already registered for account {}",
					lastMovementOfPeriod.getAccountId());
			throw e;
		} catch (RuntimeException e) {
			log.error(
					"[BillingPeriodServiceImpl:processNewBillingCycle] "
					+ "Error proccesing new billing cycle for account {}: {}",
					lastMovementOfPeriod.getAccountId(), e.getMessage());
			e.printStackTrace();
			throw new ProcessNewBillingCycleException(
					"Error proccesing new billing cycle for account " + lastMovementOfPeriod.getAccountId());
		}
	}

	@Override
	@SneakyThrows
	public Double getCurrenBillingPeriodBalanceFromAccount(Balance lastMovementOfPeriod) {
		Long accountId = lastMovementOfPeriod.getAccountId();
		log.debug("[BillingPeriodServiceImpl:getCurrenBillingPeriodBalanceFromAccount] Started for account {}",
				accountId);

		Boolean tryOnce = false;

		do {
			BillingPeriod lastBillingPeriod = getCurrentBillingPeriodFromAccount(accountId);
			Instant lastMovementTimestampOfPeriod = lastBillingPeriod.getTimestamp();
			Long lastMovementIdOfPeriod = lastBillingPeriod.getMovementId();
			Double lastPeriodBalance = lastBillingPeriod.getBalance();
			/*
			 * get transactions count and sum
			 * of current billing period.
			 */
			BillingPeriodTransactionData transactionData = getBillingPeriodTransactionsData(
							accountId,
							lastMovementIdOfPeriod, 
							lastMovementTimestampOfPeriod);
		
			try {
				if (transactionData.getCount() >= lastBillingPeriod.getTransactionsCycle()) {
					log.debug(
							"[BillingPeriodServiceImpl:getCurrenBillingPeriodBalanceFromAccount] "
									+ "Transaction count exceeded cycle limit of {} for account {}. "
									+ "New billing period will be processed for account {}",
							lastBillingPeriod.getTransactionsCycle(), accountId, accountId);
					processNewBillingCycle(
							lastMovementOfPeriod, 
							lastBillingPeriod.getTransactionsCycle(), 
							transactionData.getBalance());

				}
				
				return lastPeriodBalance + transactionData.getBalance();
			} catch (ConstraintViolationException e) {
				log.info("[BillingPeriodServiceImpl:getCurrenBillingPeriodBalanceFromAccount] "
						+ "Possible collision of account {} billing period. Operation will be proccesed one more time.",
						accountId);
				tryOnce = true;
			} 
		} while (tryOnce);
		
		log.error("Account {} balance is being modified by more than two users at the same time", lastMovementOfPeriod.getAccountId());
		throw new ConcurrentModificationException("Account " + lastMovementOfPeriod.getAccountId() + " balance is "
				+ "being modified by more than two users at the same time");
	}

	@Override
	public BillingPeriodTransactionData getBillingPeriodTransactionsData(Long accountId, Long lastMovementIdOfPeriod,
			Instant lastMovementTimestampOfPeriod) {
		return transactionDataMapper.toDTO(balanceRepository.getBillingPeriodBalanceTransactionsCountByAccountId(
				accountId, lastMovementIdOfPeriod, lastMovementTimestampOfPeriod));
	}
  
	@Override
	public void createFirstBillingPeriodForAccount(BalanceInitializationRequest request) {
		billingRepository.save(BalanceBuilder.toInitialBillingPeriod(request));
	}

	@Override
	@SneakyThrows
	@Transactional(rollbackFor = RuntimeException.class)
	public BillingPeriod getCurrentBillingPeriodFromAccount(Long accountId) {
		List<BillingPeriod> result = billingRepository.getCurrentBillingPeriodByAccountId(accountId,
				PageRequest.of(0, 1));

		if (result == null || result.isEmpty() || result.get(0) == null) {
			log.error(
					"[BillingPeriodServiceImpl:getCurrentBillingPeriodFromAccount] "
					+ "No Billing Period found for account {}. A initial period "
					+ "will be created instead",
					accountId);
			Balance initialBalance = balanceRepository.getInitialBalanceFromAccount(accountId, PageRequest.of(0, 1))
					.get(0);
			return billingRepository.save(BalanceBuilder.toInitialBillingPeriod(initialBalance));
		}

		return result.get(0);
	}

}
