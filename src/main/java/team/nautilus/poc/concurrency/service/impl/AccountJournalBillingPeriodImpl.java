package team.nautilus.poc.concurrency.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import team.nautilus.poc.concurrency.application.dto.request.BalanceCreditRequest;
import team.nautilus.poc.concurrency.application.dto.request.BalanceDebitRequest;
import team.nautilus.poc.concurrency.application.dto.response.BalanceResponse;
import team.nautilus.poc.concurrency.application.mapper.dto.LocalDate2InstantUTCMapper;
import team.nautilus.poc.concurrency.persistence.model.Balance;
import team.nautilus.poc.concurrency.persistence.repository.BalanceRepository;
import team.nautilus.poc.concurrency.service.AccountJournal;
import team.nautilus.poc.concurrency.service.AccountJournalBillingPeriod;
import team.nautilus.poc.concurrency.service.BillingPeriodService;

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
	public BalanceResponse takeFundsFromAccount(BalanceDebitRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BalanceResponse addFundsToAccount(BalanceCreditRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Balance> getLastMovementFromAllAccounts() {
		return getRepository().getLastMovementFromAllAccounts();
	}

}
