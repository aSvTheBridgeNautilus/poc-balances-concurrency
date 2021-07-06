package team.nautilus.poc.concurrency.application.facade.impl;

import java.time.Instant;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import team.nautilus.poc.concurrency.application.facade.BillingPeriodFacade;
import team.nautilus.poc.concurrency.infrastructure.errors.exceptions.ProcessNewBillingCycleException;
import team.nautilus.poc.concurrency.persistence.model.Balance;
import team.nautilus.poc.concurrency.persistence.model.BillingPeriod;
import team.nautilus.poc.concurrency.service.AccountJournal;
import team.nautilus.poc.concurrency.service.BillingPeriodService;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingPeriodFacadeImpl implements BillingPeriodFacade {

	private final BillingPeriodService billingPeriodService;
	private final AccountJournal journalService;

}
