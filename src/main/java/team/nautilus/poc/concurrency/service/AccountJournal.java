package team.nautilus.poc.concurrency.service;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import team.nautilus.poc.concurrency.application.dto.request.BalanceCreditRequest;
import team.nautilus.poc.concurrency.application.dto.request.BalanceDebitRequest;
import team.nautilus.poc.concurrency.application.dto.response.BalanceResponse;
import team.nautilus.poc.concurrency.infrastructure.errors.exceptions.InsufficientFundsException;
import team.nautilus.poc.concurrency.persistence.model.Balance;
import team.nautilus.poc.concurrency.persistence.repository.BalanceRepository;

@Slf4j
@Getter
@RequiredArgsConstructor
public abstract class AccountJournal {
	
	private final BalanceRepository repository;
	
	public abstract BalanceResponse takeFundsFromAccount(BalanceDebitRequest request);

	public abstract BalanceResponse addFundsToAccount(BalanceCreditRequest request);
	
	@SneakyThrows
	public Balance getLastMovementFromAccount(Long id) {
		List<Balance> lastMovements = getLastMovementsFromAccount(id, 0, 1);

		if (lastMovements == null || lastMovements.isEmpty() || lastMovements.get(0) == null) {
			throw new EntityNotFoundException("No movements found for account " + id);
		}

		log.info("[AccountJournal: getLastMovementFromAccount]: {}", lastMovements.get(0).toString());
		return lastMovements.get(0);
	}

	@SneakyThrows
	public boolean verifyAccountHasSufficientFunds(Long accountId, Double amountRequired){

		Balance lastMovement = getLastMovementFromAccount(accountId);

		if (amountRequired > lastMovement.getBalance()) {
			log.debug("[AccountJournal:verifyAccountHasSufficientFunds] Insufficient funds on account " + accountId);
			throw new InsufficientFundsException("Insufficient funds on account " + accountId);
		}

		return true;
	}

	public List<Balance> getLastMovementsFromAccount(Long id, int offset, int limit) {
		List<Balance> balances = repository.findAllByAccountId(id,
				PageRequest.of(offset, limit, Sort.by(Sort.Direction.DESC, "timestamp")));
		log.info("[AccountJournal: getLastMovementsFromAccount]: {}", balances);
		return balances;
	}
}
