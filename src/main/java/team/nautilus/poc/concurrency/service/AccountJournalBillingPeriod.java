package team.nautilus.poc.concurrency.service;

import java.util.List;

import org.springframework.stereotype.Service;

import team.nautilus.poc.concurrency.persistence.model.Balance;

@Service
public interface AccountJournalBillingPeriod {
	
	Balance getLastMovementFromAccount(Long accountId);

	List<Balance> getLastMovementFromAllAccounts();

}
