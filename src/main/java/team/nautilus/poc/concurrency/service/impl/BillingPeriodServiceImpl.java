package team.nautilus.poc.concurrency.service.impl;

import static team.nautilus.poc.concurrency.infrastructure.config.BillingPeriodAsyncConfiguration.BILLING_PERIOD_TASK_EXECUTOR;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import team.nautilus.poc.concurrency.application.dto.BillingPeriodTransactionData;
import team.nautilus.poc.concurrency.application.dto.builder.BalanceBuilder;
import team.nautilus.poc.concurrency.application.dto.request.BalanceInitializationRequest;
import team.nautilus.poc.concurrency.application.mapper.dto.BillingPeriodTransactionDataMapper;
import team.nautilus.poc.concurrency.application.mapper.dto.LocalDate2InstantUTCMapper;
import team.nautilus.poc.concurrency.infrastructure.errors.exceptions.AsyncCalculationException;
import team.nautilus.poc.concurrency.infrastructure.errors.exceptions.ProcessNewBillingCycleException;
import team.nautilus.poc.concurrency.persistence.model.BillingPeriod;
import team.nautilus.poc.concurrency.persistence.repository.BalanceRepository;
import team.nautilus.poc.concurrency.persistence.repository.BillingPeriodRepository;
import team.nautilus.poc.concurrency.service.BillingPeriodService;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingPeriodServiceImpl implements BillingPeriodService {

	private final BillingPeriodRepository billPeriodRepository;
	private final BalanceRepository balanceRepository;
	private final LocalDate2InstantUTCMapper utcMapper;
	private final BillingPeriodTransactionDataMapper transactionDataMapper;

	@Override
	public LocalDate getCurrentBillingPeriodDate(Long accountId) {
		log.debug(
				"[BillingPeriodServiceImpl:getCurrentBillingPeriodDate] get start date of billing period for account {}",
				accountId);
		return billPeriodRepository.getCurrentBillingDateByAccountId(accountId);
	}

	@Override
	@SneakyThrows
	public CompletableFuture<Double> getCurrenBillingPeriodBalanceFromAccount(Long accountId) {
		log.debug("[BillingPeriodServiceImpl:getCurrenBillingPeriodBalanceFromAccount] Started for account {}",
				accountId);

		return CompletableFuture.supplyAsync(() -> {
			final long start = System.currentTimeMillis();
			try {

				log.debug("[BillingPeriodServiceImpl:getCurrenBillingPeriodBalanceFromAccount] Async calculation "
						+ "of billing period balance for account {}", accountId);
				
				boolean retryOnce = false;

				do {
					try {
					// get current billing date from account
					BillingPeriod lastBillingPeriod = getCurrentBillingPeriodFromAccount(accountId);
					Instant lastPeriodInstant = lastBillingPeriod.getTimestamp();
					Double lastPeriodBalance = lastBillingPeriod.getBalance();

					BillingPeriodTransactionData transactionData = transactionDataMapper.toDTO(balanceRepository
							.getBillingPeriodBalanceTransactionsCountByAccountId(accountId, lastPeriodInstant));

				
						if (transactionData.getCount() > lastBillingPeriod.getTransactionsCycle()) {
							log.info("[BillingPeriodServiceImpl:getCurrenBillingPeriodBalanceFromAccount] "
									+ "Transactions Overflow. Billing period limit is of " + "account " + accountId
									+ " is " + transactionData.getCount() + ". New billing period will be created.");

							processNewBillingCycle(lastBillingPeriod, transactionData.getBalance(), true);

						}

						// sum billing balance and current movements
						return lastPeriodBalance + transactionData.getBalance();
					} catch (ProcessNewBillingCycleException e) {
						log.error("[BillingPeriodServiceImpl:getCurrenBillingPeriodBalanceFromAccount] "
								+ e.getMessage() + " Executor will retry operation once more");
						retryOnce = true;
					}
				} while (retryOnce);

			}  catch (RuntimeException ex) {
				log.error("[BillingPeriodServiceImpl:getCurrenBillingPeriodBalanceFromAccount] " + ex.getMessage());
				ex.printStackTrace();
				throw new AsyncCalculationException("Error in async calculation of balance for account " + accountId);
			} finally {
				log.info("[BillingPeriodServiceImpl:updateBillingPeriods] Elapsed time for calculation: {}",
						(System.currentTimeMillis() - start));
			}
			return null;
		});

	}

	@Override
	public List<BillingPeriod> getAllBillingPeriodsForUpdate() {
		log.debug("[BillingPeriodServiceImpl:getAllBillingPeriodsForUpdate] task started");
		return billPeriodRepository.getAllLatestBillingPeriods().stream()
		// discards billing periods of the current month
//				  .filter(b -> b.getId().getDate().isBefore(LocalDate.now().withDayOfMonth(1)))
				.filter(b -> b.getTimestamp()
						.isBefore(Instant.parse(LocalDate.now().atStartOfDay().withDayOfMonth(1).toString() + ":00Z")))
				.collect(Collectors.toList());
	}

	@Override
	@SneakyThrows
	@Async(BILLING_PERIOD_TASK_EXECUTOR)
	@Transactional(rollbackFor = RuntimeException.class)
	public void processNewBillingCycle(BillingPeriod currentPeriod, Double currentBalance, boolean isTransactionCycle) {
		log.debug("[BillingPeriodServiceImpl:processNewBillingCycle] Started for account {}",
				currentPeriod.getAccountId());
		try {
			BillingPeriod newPeriod = BillingPeriod.builder().accountId(currentPeriod.getAccountId())
					.userId(currentPeriod.getUserId())
					.timestamp(isTransactionCycle 
							? Instant.now()
							: Instant.parse(LocalDate.now()
									.withDayOfMonth(currentPeriod.getBillingDay()).atStartOfDay().toString() + ":00Z"))
					.billingDay(currentPeriod.getBillingDay())
					.billingCycle(currentPeriod.getBillingCycle())
					.transactionsCycle(currentPeriod.getTransactionsCycle())
					.count(0l) // new billing period always start at zero
					.balance(currentBalance).build();

			billPeriodRepository.save(newPeriod);
		} catch (ConstraintViolationException e) {
			log.error("[BillingPeriodServiceImpl:processNewBillingCycle] Billing cycle already registered for account {}",
					currentPeriod.getAccountId());
			throw new ProcessNewBillingCycleException(
					"Billing cycle already registered for account " + currentPeriod.getAccountId());
		} catch (RuntimeException e) {
			log.error(
					"[BillingPeriodServiceImpl:processNewBillingCycle] Error proccesing new billing cycle for account {}: {}",
					currentPeriod.getAccountId(), e.getMessage());
			e.printStackTrace();
			throw new ProcessNewBillingCycleException(
					"Error proccesing new billing cycle for account " + currentPeriod.getAccountId());
		}
	}

	@Override
	public void createFirstBillingPeriodForAccount(BalanceInitializationRequest request) {
		billPeriodRepository.save(BalanceBuilder.toInitialBillingPeriod(request));
	}

	@Override
	@SneakyThrows
	public BillingPeriod getCurrentBillingPeriodFromAccount(Long accountId) {
		List<BillingPeriod> result = billPeriodRepository.getCurrentBillingPeriodByAccountId(accountId,
				PageRequest.of(0, 1));

		if (result == null || result.isEmpty() || result.get(0) == null) {
			throw new EntityNotFoundException();
		}

		return billPeriodRepository.getCurrentBillingPeriodByAccountId(accountId, PageRequest.of(0, 1)).get(0);
	}

}
