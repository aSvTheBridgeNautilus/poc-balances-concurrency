package team.nautilus.poc.concurrency.service.impl;

import java.util.List;

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
import team.nautilus.poc.concurrency.persistence.model.Balance;
import team.nautilus.poc.concurrency.persistence.model.BillingPeriod;
import team.nautilus.poc.concurrency.persistence.model.embeddable.BillingPeriodId;
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
	public synchronized BillingPeriod processNewBillingCycle(Balance processedMovement, BillingPeriod lastPeriod) {
		log.info("[BillingPeriodServiceImpl:processNewBillingCycle] "
				+ "Started for account {}, after transaction {} "
				+ "was processed", 
				processedMovement.getAccountId(),
				processedMovement.getId()
				);
				
		Long accountId = processedMovement.getAccountId();
		try {
			
			if(lastPeriod.getTransactionCount() < lastPeriod.getTransactionsCycle()) {
				log.info("[BillingPeriodServiceImpl:processNewBillingCycle] "
						+ "Transaction limit for period {} of account {} "
						+ "hasn't been reached",
						lastPeriod.getId().getMovementId(),
						accountId);
				return null;
			}
		
			
			BillingPeriod newPeriod = BillingPeriod.builder()
					.id(BillingPeriodId
						.builder()
						.accountId(accountId)
						.userId("user" + accountId + "@nautilus.team")
						.movementId(processedMovement.getId())
						.build())
					.transactionsCycle(lastPeriod.getTransactionsCycle())
					// balance will be set onward
					.build();
			
			/*
			 * Before create a new billing cycle, 
			 * let's make sure the transaction cycle 
			 * on the current period has been 
			 * exhausted.
			 */
			Long totalTransactions = null;
			if ((totalTransactions = getTotalTransactionsFromCurrentBillingPeriod(accountId)) < newPeriod.getTransactionsCycle()) {
				log.info("[BillingPeriodServiceImpl:processNewBillingCycle] "
						+ "Current billing period of account {} hasn't "
						+ "exhausted transactions limit of {}, current "
						+ "transactions: {}. Operation aborted",
						accountId, 
						newPeriod.getTransactionsCycle(),
						totalTransactions);
				return null;
			}
			
			newPeriod.setBalance(
					lastPeriod.getBalance() 
					+ getBalanceBetweenMovementsFromAccount(
							accountId, 
							lastPeriod.getId().getMovementId(), 
							newPeriod.getId().getMovementId()));

		    return billingRepository.saveAndFlush(newPeriod);
		} catch (RuntimeException e) {
			log.error("[BillingPeriodServiceImpl:processNewBillingCycle] "
					 + "Error proccesing new billing cycle for account {}: {}",
					processedMovement.getAccountId(), e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	@SneakyThrows
	public void verifyTransactionCycleIsExhaustedFor(BillingPeriod currentPeriod) {
		if (getTotalTransactionsFromCurrentBillingPeriod(currentPeriod.getId().getAccountId()) 
				< currentPeriod.getTransactionsCycle()) {
			log.error("[BillingPeriodServiceImpl:getCurrenBillingPeriodBalanceFromAccount] "
					+ "Processing new Billing Period for account {} was aborted. "
					+ "Transaction limit for previou period until movement {}, has not been "
					+ "exhausted", currentPeriod.getId().getAccountId(), currentPeriod.getId().getMovementId());
			throw new BillingPeriodOutdatedException("[BillingPeriodServiceImpl:getCurrenBillingPeriodBalanceFromAccount] "
					+ "Processing new Billing Period for account "
					+ currentPeriod.getId().getAccountId()
					+ " was aborted. "
					+ "Transaction limit for previou period until movement "
					+ currentPeriod.getId().getMovementId()
					+ ", has not been "
					+ "exhausted");
		}
	}

	@Override
	@SneakyThrows
	public Long getLatestMovementIdFromAccountBillingPeriods(Long accountId) {
		return billingRepository.getLatestMovementIdFromAccountBillingPeriodsByAccountId(accountId);
	}

	@Override
	@SneakyThrows
	public Double getBalanceBetweenMovementsFromAccount(Long accountId, Long fromMovementId, Long toMovementId) {
		return balanceRepository.sumTransactionsAmountBetweenIdsByAccountId(accountId, fromMovementId, toMovementId);
	}
	

	@Override
	@SneakyThrows
	public Long getTotalTransactionsFromBillingPeriod(BillingPeriod period) {
		return balanceRepository.countTransactionsFromBillingPeriodByAccountId(period.getId().getAccountId(), period.getId().getMovementId());
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

		BillingPeriod lastBillingPeriod = getLastBillingPeriodFromAccountIfNotFoundCreateInitial(accountId);

		BillingPeriodTransactionData transactionData = getTransactionsDataFromBillingPeriodOnward(
				accountId,
				lastBillingPeriod.getId().getMovementId());

		lastBillingPeriod.setCacheBalance(lastBillingPeriod.getBalance() + transactionData.getBalance());
		lastBillingPeriod.setTransactionCount(transactionData.getCount());

		return lastBillingPeriod;	
	}

	@Override
	public BillingPeriodTransactionData getTransactionsDataFromBillingPeriodOnward(Long accountId, Long lastMovementIdOfPeriod) {
		return transactionDataMapper.toDTO(balanceRepository.sumAmountCountTransactionFromBillingPeriodByAccountId(
				accountId, 
				lastMovementIdOfPeriod));
	}
	
	@Override
	public BillingPeriodTransactionData getTransactionsDataBetweenMovements(Long accountId, Long fromId, Long toId) {
		return transactionDataMapper.toDTO(balanceRepository.sumAmountCountTransactionsBetweenMovementsByAccountId(
				accountId, 
				fromId, 
				toId));
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
