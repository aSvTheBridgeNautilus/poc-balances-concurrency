package team.nautilus.poc.concurrency.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import team.nautilus.poc.concurrency.application.mapper.dto.LocalDate2InstantUTCMapper;
import team.nautilus.poc.concurrency.infrastructure.errors.exceptions.InsufficientFundsException;
import team.nautilus.poc.concurrency.persistence.model.Balance;
import team.nautilus.poc.concurrency.persistence.repository.BalanceRepository;
import team.nautilus.poc.concurrency.service.AccountJournal;
import team.nautilus.poc.concurrency.service.AccountJournalBillingPeriod;
import team.nautilus.poc.concurrency.service.BillingPeriodService;

@Slf4j
@Service
public class AccountJournalBillingPeriodImpl extends AccountJournal implements AccountJournalBillingPeriod {

	public AccountJournalBillingPeriodImpl(
			BalanceRepository repository, 
			LocalDate2InstantUTCMapper dateUTCMapper,
			BillingPeriodService billingPeriodService) {
		super(repository, dateUTCMapper, billingPeriodService);
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<Balance> getLastMovementFromAllAccounts() {
		return getRepository().getLastMovementFromAllAccounts();
	}
	
	@Override
	@SneakyThrows
	public boolean verifyAccountHasSufficientFunds(Long accountId, Double amountRequired) {
		if (amountRequired > getBillingPeriodService().getCurrenBillingPeriodBalanceFromAccount(accountId).get()) {
			log.error("[AccountJournal:verifyAccountHasSufficientFunds] Insufficient funds on account " + accountId);
			throw new InsufficientFundsException("Insufficient funds on account " + accountId);
		}

		return true;
	}


}
