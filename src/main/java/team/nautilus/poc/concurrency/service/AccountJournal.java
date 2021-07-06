package team.nautilus.poc.concurrency.service;

import java.util.List;

import org.springframework.stereotype.Service;

import team.nautilus.poc.concurrency.application.dto.request.BalanceCreditRequest;
import team.nautilus.poc.concurrency.application.dto.request.BalanceDebitRequest;
import team.nautilus.poc.concurrency.application.dto.response.BalanceResponse;
import team.nautilus.poc.concurrency.persistence.model.Balance;

@Service
public interface AccountJournal {

	Balance getLastMovementFromAccount(Long accountId);

	List<Balance> getLastMovementFromAllAccounts();

	boolean persistMovementsInSingleTransaction(Balance source, Balance target);

	boolean checkMovementsExistInDB(Long sourceMovId, Long targetMovId);

	List<Balance> getLastMovementsFromAccount(Long id, int offset, int limit);

	Balance getBalanceFromAccount(Long id);
	
	Long generateNewMovementIdForAccount(Long accountId);

}
