package team.nautilus.poc.concurrency.service.impl;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.aop.interceptor.AsyncExecutionInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import team.nautilus.poc.concurrency.application.dto.builder.BalanceBuilder;
import team.nautilus.poc.concurrency.application.dto.request.BalanceInitializationRequest;
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

	@Override
	public LocalDate getCurrentBillingPeriodDate(Long accountId) {
		log.debug("[BillingPeriodServiceImpl:getCurrentBillingPeriodDate] get start date of billing period for account {}", accountId);
		return billPeriodRepository.getCurrentBillingDateByAccountId(accountId);
	}

	@Override
	@SneakyThrows
	public CompletableFuture<Double> getCurrenBillingPeriodBalanceFromAccount(Long accountId) {
		log.debug("[BillingPeriodServiceImpl:getCurrenBillingPeriodBalanceFromAccount] Started for account {}", accountId);

		return CompletableFuture.supplyAsync(() -> {
			final long start = System.currentTimeMillis();
			try {
				
				log.debug("[BillingPeriodServiceImpl:getCurrenBillingPeriodBalanceFromAccount] Async calculation "
						+ "of billing period balance for account {}",
						accountId);
				// get current billing date from account
				BillingPeriod currentBillingPeriod = billPeriodRepository.getCurrentBillingPeriodByAccountId(accountId);
				Instant currentBillingInstant = utcMapper.toDTO(currentBillingPeriod.getBillingDate());
				Double billingBalance = currentBillingPeriod.getBalance();
				// sum billing balance and current movements

				return billingBalance
						+ balanceRepository.getCurrentBillingPeriodBalanceByAccountId(accountId, currentBillingInstant);
				
			} catch(RuntimeException ex){
				log.error("[BillingPeriodServiceImpl:getCurrenBillingPeriodBalanceFromAccount] " + ex.getMessage());
				throw new AsyncCalculationException("Error in async calculation of balance for account " + accountId);
			} finally {
				log.info("[BillingPeriodServiceImpl:updateBillingPeriods] Elapsed time for calculation: {}",
						(System.currentTimeMillis() - start));
			}
		});
	      
	}

	@Override
	public List<BillingPeriod> getAllBillingPeriodsForUpdate() {
		log.debug("[BillingPeriodServiceImpl:getAllBillingPeriodsForUpdate] task started");
		return billPeriodRepository.getAllLatestBillingPeriods()
				  .stream()
				  // discards billing periods of the current month
//				  .filter(b -> b.getId().getDate().isBefore(LocalDate.now().withDayOfMonth(1)))
				  .filter(b -> b.getBillingDate().isBefore(LocalDate.now().withDayOfMonth(1)))
				  .collect(Collectors.toList());
	}

	@Override
	@SneakyThrows
	@Transactional(rollbackFor = RuntimeException.class)
	public void processNewBillingCycle(BillingPeriod currentPeriod, Double currentBalance) {
		log.debug("[BillingPeriodServiceImpl:processNewBillingCycle] Started for account {}", currentPeriod.getAccountId());
      	try {
			BillingPeriod newPeriod = BillingPeriod
					.builder()
					.accountId(currentPeriod.getAccountId())
					.userId(currentPeriod.getUserId())
					.billingDate(LocalDate.now().withDayOfMonth(currentPeriod.getBillingDay()))
					.billingDay(currentPeriod.getBillingDay())
					.billingCycle(currentPeriod.getBillingCycle())
					.balance(currentBalance)
					.build();
			
			billPeriodRepository.save(newPeriod);
		} catch (RuntimeException e) {
			log.error("[BillingPeriodServiceImpl:processNewBillingCycle] Error proccesing new billing cycle for account {}: {}", currentPeriod.getAccountId(), e.getMessage());
			throw new ProcessNewBillingCycleException("Error proccesing new billing cycle for account " + currentPeriod.getAccountId());
		}
	}

	@Override
	public void createFirstBillingPeriodForAccount(BalanceInitializationRequest request) {
		billPeriodRepository.save(BalanceBuilder.toInitialBillingPeriod(request));
	}

}
