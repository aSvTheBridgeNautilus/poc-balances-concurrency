package team.nautilus.poc.concurrency.service.impl;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import team.nautilus.poc.concurrency.application.dto.request.BalanceCreditRequest;
import team.nautilus.poc.concurrency.application.dto.request.BalanceDebitRequest;
import team.nautilus.poc.concurrency.application.dto.response.BalanceResponse;
import team.nautilus.poc.concurrency.application.mapper.dto.LocalDate2InstantUTCMapper;
import team.nautilus.poc.concurrency.persistence.model.Balance;
import team.nautilus.poc.concurrency.persistence.repository.BalanceRepository;
import team.nautilus.poc.concurrency.service.AccountJournal;
import team.nautilus.poc.concurrency.service.BillingPeriodService;

@Slf4j
@RequiredArgsConstructor
@Service
public class AccountJournalImpl implements AccountJournal {
	
	private final BalanceRepository balanceRepository;
	private final LocalDate2InstantUTCMapper dateUTCMapper;
	private final BillingPeriodService billingPeriodService;

	@Override
	public List<Balance> getLastMovementFromAllAccounts() {
		return balanceRepository.getLastMovementFromAllAccounts();
	}

	@SneakyThrows
	public Balance getBalanceFromAccount(Long id) {
		List<Balance> lastMovements = getLastMovementsFromAccount(id, 0, 1);

		if (lastMovements == null || lastMovements.isEmpty() || lastMovements.get(0) == null) {
			throw new EntityNotFoundException("No movements found for account " + id);
		}

		log.info("[AccountJournal: getLastMovementFromAccount]: {}", lastMovements.get(0).toString());
		return lastMovements.get(0);
	}

	@SneakyThrows
	public Balance getLastMovementFromAccount(Long id) {
		List<Balance> lastMovements = getLastMovementsFromAccount(id, 0, 1);

		if (lastMovements == null || lastMovements.isEmpty() || lastMovements.get(0) == null) {
			throw new EntityNotFoundException("No movements found for account " + id);
		}

		log.info("[AccountJournal: getLastMovementFromAccount]: {}", lastMovements.get(0).toString());
		return lastMovements.get(0);
	}

	public List<Balance> getLastMovementsFromAccount(Long id, int offset, int limit) {
		List<Balance> balances = balanceRepository.findAllByAccountId(id,
				PageRequest.of(offset, limit, Sort.by(Sort.Direction.DESC, "timestamp")));
		log.info("[AccountJournal: getLastMovementsFromAccount]: {}", balances);
		return balances;
	}

}
