package team.nautilus.poc.concurrency.application.facade.impl;

import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import team.nautilus.poc.concurrency.application.facade.BillingPeriodFacade;
import team.nautilus.poc.concurrency.persistence.model.BillingPeriod;
import team.nautilus.poc.concurrency.service.AccountJournalBillingPeriod;
import team.nautilus.poc.concurrency.service.BillingPeriodService;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingPeriodFacadeImpl implements BillingPeriodFacade {

	private final BillingPeriodService billingPeriodService;
	private final AccountJournalBillingPeriod journalService;
	
	@Async
	@SneakyThrows
    public void updateBillingPeriods() {
		log.debug("[BillingPeriodServiceImpl:updateBillingPeriods] Task started");
        final long start = System.currentTimeMillis();

        List<BillingPeriod> periods = billingPeriodService.getAllBillingPeriodsForUpdate();
//        List<Balance> lastMovements = journalService.getLastMovementFromAllAccounts();
        
        for(BillingPeriod currentPeriod : periods) {
        	billingPeriodService.processNewBillingCycle(
        			currentPeriod, 
        			journalService.getCurrentBillingPeriodBalanceByAccountId(currentPeriod.getAccountId()));
        }

        log.info("[BillingPeriodServiceImpl:updateBillingPeriods] Elapsed time: {}", (System.currentTimeMillis() - start));
    }
}
