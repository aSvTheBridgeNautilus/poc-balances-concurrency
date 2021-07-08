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
			/*
			 * Before create a new billing cycle,
			 * let's make sure the transaction 
			 * cycle on the current period has
			 * been exhausted.
			 */
			verifyTransactionCycleIsExhaustedFor(lastPeriod);

			return billingRepository.saveAndFlush(newPeriod);
		} catch (BillingPeriodOutdatedException e) {
			log.error(e.getMessage());
			throw e;
//		} catch (DataIntegrityViolationException e) {
//			log.error("[BillingPeriodServiceImpl:processNewBillingCycle] "
//					+ "Billing cycle already registered for account {}",
//					lastMovementOfPeriod.getAccountId());
//			throw e;
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
	public void verifyTransactionCycleIsExhaustedFor(BillingPeriod currentPeriod) {
		if (getTotalTransactionsFromCurrentBillingPeriod(currentPeriod.getAccountId()) < currentPeriod.getTransactionsCycle()) {
			log.error("[BillingPeriodServiceImpl:getCurrenBillingPeriodBalanceFromAccount] "
					+ "Billing period transaction limit of account {} has not been "
					+ "exhausted. Operation aborted", currentPeriod.getAccountId());
			throw new BillingPeriodOutdatedException("[BillingPeriodServiceImpl:getCurrenBillingPeriodBalanceFromAccount] "
					+ "Billing period transaction limit of account "
					+ currentPeriod.getAccountId()
					+ " has not been exhausted. Operation aborted");
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
		return balanceRepository.countTransactionsFromBillingPeriodByAccountId(period.getAccountId(), period.getMovementId());
	}
	
	@Override
	@SneakyThrows
	public Long getTotalTransactionsFromCurrentBillingPeriod(Long accountId) {
		return balanceRepository.countTransactionsFromCurrentBillingPeriodByAccountId(accountId);
	}
	
	@Override
	@SneakyThrows
	public BillingPeriod getCurrentBillingPeriodFromAccount(Balance lastMovementOfPeriod) {
		Long accountId = lastMovementOfPeriod.getAccountId();
		log.debug("[BillingPeriodServiceImpl:getCurrenBillingPeriodBalanceFromAccount] Started for account {}",
				accountId);

		Long currentMovementId = null;

		BillingPeriod lastBillingPeriod = getLastBillingPeriodFromAccountIfNotFoundCreateInitial(accountId);

		Instant lastMovementTimestampOfPeriod = lastBillingPeriod.getTimestamp();
		Long lastMovementIdOfPeriod = lastBillingPeriod.getMovementId();
		Double lastPeriodBalance = lastBillingPeriod.getBalance();
		/*
		 * get transactions count and sum of current billing period.
		 */
		BillingPeriodTransactionData transactionData = getBillingPeriodTransactionsData(
				accountId,
				lastMovementIdOfPeriod, 
				lastMovementTimestampOfPeriod);

		/*
		 * This would be the latest updated balance calculated during this process but
		 * still not saved in a billing period on DB.
		 */
		Double cacheBalance = lastPeriodBalance + transactionData.getBalance();

		try {
			if (transactionData.getCount() >= lastBillingPeriod.getTransactionsCycle()) {
				log.debug(
						"[BillingPeriodServiceImpl:getCurrenBillingPeriodBalanceFromAccount] "
								+ "Transaction count exceeded cycle limit of {} for account {}. "
								+ "New billing period will be processed for account {}",
						lastBillingPeriod.getTransactionsCycle(), accountId, accountId);
				lastBillingPeriod = processNewBillingCycle(lastMovementOfPeriod, lastBillingPeriod, cacheBalance);

			}
		} catch (BillingPeriodOutdatedException e) {
			log.info(e.getMessage());
		} catch (RuntimeException e) {
			log.error("Account {} balance is being modified by more than two users at the same time", lastMovementOfPeriod.getAccountId());
			e.printStackTrace();
			throw new ConcurrentModificationException("Account " + lastMovementOfPeriod.getAccountId() + " balance is "
					+ "being modified by more than two users at the same time");
			
		}

		lastBillingPeriod.setCacheBalance(cacheBalance);

		return lastBillingPeriod;		
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
