package team.nautilus.poc.concurrency.service.impl;

import java.time.Instant;
import java.util.List;

import javax.naming.OperationNotSupportedException;
import javax.persistence.EntityNotFoundException;
import javax.persistence.LockModeType;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import team.nautilus.poc.concurrency.application.dto.builder.BalanceBuilder;
import team.nautilus.poc.concurrency.application.dto.request.BalanceCreditRequest;
import team.nautilus.poc.concurrency.application.dto.request.BalanceDebitRequest;
import team.nautilus.poc.concurrency.application.dto.response.BalanceResponse;
import team.nautilus.poc.concurrency.infrastructure.errors.exceptions.InsufficientFundsException;
import team.nautilus.poc.concurrency.persistence.repository.BalanceRepository;
import team.nautilus.poc.concurrency.persistence.repository.model.Balance;
import team.nautilus.poc.concurrency.service.AccountJournal;

@Slf4j
@RequiredArgsConstructor
@Service
public class AccountJournalImpl implements AccountJournal {

	private final BalanceRepository repository;

	@Override
	@SneakyThrows
	@Transactional(rollbackFor = RuntimeException.class)
	@Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
	public BalanceResponse takeFundsFromAccount(BalanceDebitRequest request) {
		log.debug("[AccountJournalFacade:takeFundsFromAccount] started...");
		
		verifyAccountHasSufficientFunds(request.getAccountId(), request.getAmount());
		
		/*
		 * take funds
		 */
		
		try {
			
			Balance lastMovement = getLastMovementFromAccount(request.getAccountId());
			Balance debitMovement =  Balance.builder()
					.accountId(lastMovement.getAccountId())
					.amount(request.getAmount())
					.balance(lastMovement.getBalance() - request.getAmount())
					.accountId(lastMovement.getAccountId())
					.timestamp(Instant.now())
					.build();
			
			Long currentVersion = null;
			
			while (lastMovement.getVersion() 
					!= (currentVersion = repository.getCurrentBalanceVersionByAccountId(lastMovement.getAccountId()))) {
				
				log.info("[AccountJournal: debitMovement] "
						+ "not same versions: {} - {}. "
						+ "Waiting for other updates to "
						+ "finish...",
						lastMovement.getVersion(), 
						currentVersion);
				
				/*
				 * wait a bit
				 */
				
				Thread.sleep(350);
				
				lastMovement = getLastMovementFromAccount(request.getAccountId());
				debitMovement = Balance.builder()
						.accountId(lastMovement.getAccountId())
						.amount(request.getAmount())
						.balance(lastMovement.getBalance() - request.getAmount())
						.accountId(lastMovement.getAccountId())
						.timestamp(Instant.now())
						.build();
			}
			
			log.info("[AccountJournal: debitMovement] save new movement for transfer {}",
					request.getTransferReferenceId());
			
			Balance balance = repository.save(debitMovement);
			
			log.info("[AccountJournal: debitMovement] {}", balance);
			
			return  BalanceBuilder.toResponse(balance);
		} catch (Exception ex) {
			throw new OperationNotSupportedException("Couldn't take funds from account " + request.getAccountId());
		}
		
	}
	
	@Override
	@SneakyThrows
	@Transactional(rollbackFor = Exception.class)
	public BalanceResponse addFundsToAccount(BalanceCreditRequest request) {
		try {
			
			Balance lastMovement = getLastMovementFromAccount(request.getAccountId());
			
			Balance creditMovement = Balance.builder()
					.accountId(lastMovement.getAccountId())
					.amount(request.getAmount())
					.balance(lastMovement.getBalance() + request.getAmount())
					.accountId(lastMovement.getAccountId())
					.timestamp(Instant.now())
					.build();
			
			Balance balance = repository.save(creditMovement);
			
			log.info("[AccountJournal: addFundsToAccountFrom]: {}", balance);
			
			return  BalanceBuilder.toResponse(balance);
		} catch (Exception ex) {
			throw new OperationNotSupportedException(
					"Couldn't transfer funds to account " + request.getAccountId());
		}
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
