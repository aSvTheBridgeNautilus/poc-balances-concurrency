package team.nautilus.poc.concurrency.application.facade.impl;

import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import team.nautilus.poc.concurrency.application.facade.BillingPeriodFacade;
import team.nautilus.poc.concurrency.persistence.model.BillingPeriod;
import team.nautilus.poc.concurrency.service.AccountJournal;
import team.nautilus.poc.concurrency.service.BillingPeriodService;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingPeriodFacadeImpl implements BillingPeriodFacade {

	private final BillingPeriodService billingPeriodService;
	private final AccountJournal journalService;
	
	@Async
	@Override
	@SneakyThrows
    public void updateBillingPeriods() {
		log.debug("[BillingPeriodServiceImpl:updateBillingPeriods] Task started");
        final long start = System.currentTimeMillis();

        List<BillingPeriod> periods = billingPeriodService.getAllBillingPeriodsForUpdate();
        
        for(BillingPeriod currentPeriod : periods) {
        	try {
				billingPeriodService.processNewBillingCycle(
						currentPeriod, 
						// this step will wait if calculation isn't over
						billingPeriodService.getCurrenBillingPeriodBalanceFromAccount(currentPeriod.getAccountId()).get());
			} catch (Exception e) {
				/*
				 * we catch and log the exception
				 * but we continue with the next 
				 * items.
				 */
				log.error("[BillingPeriodServiceImpl:updateBillingPeriods] Error updating billing "
						+ "period of account "
						+ currentPeriod.getAccountId()
						+ ", cause: " + e.getLocalizedMessage());
			}
        }

        log.info("[BillingPeriodServiceImpl:updateBillingPeriods] Elapsed time: {}", (System.currentTimeMillis() - start));
    }
}
