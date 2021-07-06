package team.nautilus.poc.concurrency.service.impl;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import team.nautilus.poc.concurrency.infrastructure.errors.exceptions.CreateMovementsException;
import team.nautilus.poc.concurrency.persistence.model.Balance;
import team.nautilus.poc.concurrency.persistence.repository.BalanceRepository;
import team.nautilus.poc.concurrency.service.AccountJournal;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountJournalImpl implements AccountJournal {
	
	private final BalanceRepository balanceRepository;

	@Override
	public List<Balance> getLastMovementFromAllAccounts() {
		return balanceRepository.getLastMovementFromAllAccounts();
	}

	@Override
	@SneakyThrows
	public Balance getBalanceFromAccount(Long id) {
		List<Balance> lastMovements = getLastMovementsFromAccount(id, 0, 1);

		if (lastMovements == null || lastMovements.isEmpty() || lastMovements.get(0) == null) {
			throw new EntityNotFoundException("No movements found for account " + id);
		}

		log.info("[AccountJournal: getLastMovementFromAccount]: {}", lastMovements.get(0).toString());
		return lastMovements.get(0);
	}

	@Override
	@SneakyThrows
	public Balance getLastMovementFromAccount(Long id) {
		List<Balance> lastMovements = getLastMovementsFromAccount(id, 0, 1);

		if (lastMovements == null || lastMovements.isEmpty() || lastMovements.get(0) == null) {
			throw new EntityNotFoundException("No movements found for account " + id);
		}

		log.info("[AccountJournal: getLastMovementFromAccount]: {}", lastMovements.get(0).toString());
		return lastMovements.get(0);
	}

	@Override
	public List<Balance> getLastMovementsFromAccount(Long id, int offset, int limit) {
		List<Balance> balances = balanceRepository.findAllByAccountId(id,
				PageRequest.of(offset, limit, Sort.by(Sort.Direction.DESC, "timestamp")));
		log.info("[AccountJournal: getLastMovementsFromAccount]: {}", balances);
		return balances;
	}

	@Override
	@SneakyThrows
	@Transactional(rollbackFor = { RuntimeException.class })
	public boolean persistMovementsInSingleTransaction(
			Balance source, 
			Balance target) {
		log.debug("[AccountJournal:persistMovements] started");
		try {
			balanceRepository.save(source);
			balanceRepository.save(target);
			boolean existsInDb = checkMovementsExistInDB(source.getId(), target.getId());

			return existsInDb;
		} catch (RuntimeException ex) {
			log.debug("[AccountJournal:persistMovements] movements creation error.");
			throw new CreateMovementsException(ex.getMessage());
		}
	}

	@Override
	public boolean checkMovementsExistInDB(Long sourceMovId, Long targetMovId) {
		boolean sourceMovExists = balanceRepository.findById(sourceMovId).isPresent();
		boolean targetMovExists = balanceRepository.findById(targetMovId).isPresent();

		log.debug("[AccountJournal:checkIfMovementsExistInDB] source exists: " + sourceMovExists);
		log.debug("[AccountJournal:checkIfMovementsExistInDB] target exists: " + targetMovExists);
		return sourceMovExists && targetMovExists;
	}

}
