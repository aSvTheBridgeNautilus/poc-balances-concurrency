package team.nautilus.poc.concurrency.service.impl;

import java.time.Instant;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Objects;

import org.springframework.dao.DataIntegrityViolationException;
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
import team.nautilus.poc.concurrency.infrastructure.errors.exceptions.BillingPeriodOutdatedException;
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
	public BillingPeriod processNewBillingCycle(Balance lastMovementOfPeriod, BillingPeriod lastPeriod, Double currentBalance) {
		Long accountId = lastMovementOfPeriod.getAccountId();
		log.debug("[BillingPeriodServiceImpl:processNewBillingCycle] Started for account {}",
				accountId);
		try {
			BillingPeriod newPeriod = BillingPeriod
					.builder()
					.accountId(accountId)
					.userId("user" + accountId + "@nautilus.team")
					.timestamp(lastMovementOfPeriod.getTimestamp()) 
					.movementId(lastMovementOfPeriod.getId())
					.transactionsCycle(lastPeriod.getTransactionsCycle())
					.balance(currentBalance)
					.build();
			
			/**
			 * Last check to verify
			 * another billing period 
			 * hasn't been inserted 
			 * during the creation 
			 * of this billing period
			 */
			verifyBillingPeriodIsNotOutdated(lastPeriod);

			return billingRepository.saveAndFlush(newPeriod);
		} catch (BillingPeriodOutdatedException e) {
			log.error(e.getMessage());
			throw e;
		} catch (DataIntegrityViolationException e) {
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
	public void verifyBillingPeriodIsNotOutdated(BillingPeriod period) {
//		if (newPeriod.getMovementId() <= getLatestMovementIdFromAccountBillingPeriods(newPeriod.getAccountId())) {
//			log.error("Billing period of account {} appears to be outdated. Operation aborted", newPeriod.getAccountId());
//			throw new BillingPeriodOutdatedException("Billing period of account " + newPeriod.getAccountId()
//			+ " appears to be outdated. Operation aborted");
//		}
		if (period.getTransactionsCycle() < getTotalTransactionsFromBillingPeriod(period)) {
			log.error("Billing period of account {} appears to be outdated. Operation aborted", period.getAccountId());
			throw new BillingPeriodOutdatedException("Billing period of account " + period.getAccountId()
					+ " appears to be outdated. Operation aborted");
		}
	}

	@Override
	@SneakyThrows
	public Long getLatestMovementIdFromAccountBillingPeriods(Long accountId) {
		return billingRepository.getLatestMovementIdFromAccountBillingPeriodsByAccountId(accountId);
	}

	@Override
	@SneakyThrows
	public Long getTotalTransactionsFromBillingPeriod(BillingPeriod period) {
		return balanceRepository.countTransactionOnCurrentBillingPeriodByAccountId(period.getAccountId(), period.getMovementId());
	}
	
	@Override
	@SneakyThrows
	public BillingPeriod getCurrentBillingPeriodFromAccount(Balance lastMovementOfPeriod) {
		Long accountId = lastMovementOfPeriod.getAccountId();
		log.debug("[BillingPeriodServiceImpl:getCurrenBillingPeriodBalanceFromAccount] Started for account {}",
				accountId);

		Long currentMovementId = null;
		Boolean tryAgain = false;
		int count = 0;

		do {
			if(count > 0) {
				log.info("[BillingPeriodServiceImpl:getCurrenBillingPeriodBalanceFromAccount] "
						+ "Possible excessive of concurrent operations on account {}. Operation will be proccesed once more.",
						accountId);
			}
			BillingPeriod lastBillingPeriod = getLastBillingPeriodFromAccountIfNotFoundCreateInitial(accountId);
			
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
			
			/*
			 * This would be the latest
			 * updated balance calculated
			 * during this process but
			 * still not saved in a billing
			 * period on DB.
			 */
			Double cacheBalance = lastPeriodBalance + transactionData.getBalance();
		
			try {
				if (transactionData.getCount() >= lastBillingPeriod.getTransactionsCycle()) {
					log.debug(
							"[BillingPeriodServiceImpl:getCurrenBillingPeriodBalanceFromAccount] "
									+ "Transaction count exceeded cycle limit of {} for account {}. "
									+ "New billing period will be processed for account {}",
							lastBillingPeriod.getTransactionsCycle(), accountId, accountId);
					lastBillingPeriod = processNewBillingCycle(
							lastMovementOfPeriod,
							lastBillingPeriod, 
							cacheBalance);

				}
				
				lastBillingPeriod.setCacheBalance(cacheBalance);
				
//				return lastPeriodBalance + transactionData.getBalance();
				return lastBillingPeriod;
			} catch (BillingPeriodOutdatedException e) {
				log.info("[BillingPeriodServiceImpl:getCurrenBillingPeriodBalanceFromAccount] "
						+ "Possible collision of account {} billing period. Operation will be proccesed one more time.",
						accountId);
				e.printStackTrace();
				tryAgain = true;
				count++;
			} catch (RuntimeException e) {
				log.info("[BillingPeriodServiceImpl:getCurrenBillingPeriodBalanceFromAccount] "
						+ "Possible collision of account {} billing period. Operation will be proccesed one more time.",
						accountId);
				e.printStackTrace();
				tryAgain = true;
				count++;
			} 
		} while (tryAgain && count < 3);
		
		log.error("Account {} balance is being modified by more than two users at the same time", lastMovementOfPeriod.getAccountId());
		throw new ConcurrentModificationException("Account " + lastMovementOfPeriod.getAccountId() + " balance is "
				+ "being modified by more than two users at the same time");
	}

	@Override
	public BillingPeriodTransactionData getBillingPeriodTransactionsData(Long accountId, Long lastMovementIdOfPeriod,
			Instant lastMovementTimestampOfPeriod) {
		return transactionDataMapper.toDTO(balanceRepository.sumAmountCountTransactionFromBillingPeriodByAccountId(
				accountId, 
				lastMovementIdOfPeriod
//				lastMovementTimestampOfPeriod
				));
	}
  
	@Override
	public void createFirstBillingPeriodForAccount(BalanceInitializationRequest request) {
		billingRepository.save(BalanceBuilder.toInitialBillingPeriod(request));
	}

	@Override
	@SneakyThrows
	@Transactional(rollbackFor = RuntimeException.class)
	public BillingPeriod getLastBillingPeriodFromAccountIfNotFoundCreateInitial(Long accountId) {
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
			return billingRepository.saveAndFlush(BalanceBuilder.toInitialBillingPeriod(initialBalance));
		}

		return result.get(0);
	}


}
