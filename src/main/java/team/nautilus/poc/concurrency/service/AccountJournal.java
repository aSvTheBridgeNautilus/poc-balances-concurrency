package team.nautilus.poc.concurrency.service;

import java.security.InvalidParameterException;
import java.time.Instant;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import team.nautilus.poc.concurrency.application.dto.builder.BalanceBuilder;
import team.nautilus.poc.concurrency.application.dto.request.BalanceCreditRequest;
import team.nautilus.poc.concurrency.application.dto.request.BalanceDebitRequest;
import team.nautilus.poc.concurrency.application.dto.response.BalanceResponse;
import team.nautilus.poc.concurrency.application.mapper.dto.LocalDate2InstantUTCMapper;
import team.nautilus.poc.concurrency.infrastructure.errors.exceptions.InsufficientFundsException;
import team.nautilus.poc.concurrency.persistence.model.Balance;
import team.nautilus.poc.concurrency.persistence.model.constant.TransactionType;
import team.nautilus.poc.concurrency.persistence.repository.BalanceRepository;

@Slf4j
@Getter
@RequiredArgsConstructor
public abstract class AccountJournal {
	
	private final BalanceRepository repository;
	private final LocalDate2InstantUTCMapper dateUTCMapper;
	private final BillingPeriodService billingPeriodService;
	
	@SneakyThrows
	@Transactional(rollbackFor = RuntimeException.class)
	public BalanceResponse takeFundsFromAccount(BalanceDebitRequest request) {
		log.debug("[AccountJournalFacade:takeFundsFromAccount] started...");
		
		verifyAccountHasSufficientFunds(request.getAccountId(), request.getAmount());
		
		try {
			
			Balance lastMovement = getLastMovementFromAccount(request.getAccountId());
			Balance debitMovement =  Balance.builder()
					.accountId(lastMovement.getAccountId())
					.amount(-request.getAmount())
					.balance(billingPeriodService.getCurrenBillingPeriodBalanceFromAccount(request.getAccountId()).get() - request.getAmount())
					.accountId(lastMovement.getAccountId())
					.timestamp(Instant.now())
					.type(TransactionType.TOP)
					.version(lastMovement.getVersion() + 1)
					.build();
			
			
			log.info("[AccountJournal:takeFundsFromAccount] save new movement of Account {}",
					lastMovement.getAccountId());
			
			Balance balance = getRepository().save(debitMovement);
		
			log.info("[AccountJournal:takeFundsFromAccount] {}", balance);
			
			return  BalanceBuilder.toResponse(balance);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new InvalidParameterException("Couldn't take funds from account " + request.getAccountId());
		}
	}
	
	@SneakyThrows
	@Transactional(rollbackFor = Exception.class)
	public BalanceResponse addFundsToAccount(BalanceCreditRequest request) {
		try {
			
			Balance lastMovement = getLastMovementFromAccount(request.getAccountId());
			Balance creditMovement =  Balance.builder()
					.accountId(lastMovement.getAccountId())
					.amount(request.getAmount())
					.balance(billingPeriodService.getCurrenBillingPeriodBalanceFromAccount(request.getAccountId()).get()  + request.getAmount())
					.accountId(lastMovement.getAccountId())
					.timestamp(Instant.now())
					.type(TransactionType.TOP)
					.version(lastMovement.getVersion() + 1)
					.build();

			
			log.info("[AccountJournal:addFundsToAccount] save new movement of Account {}",
					lastMovement.getAccountId());
			
			Balance balance = getRepository().save(creditMovement);

			log.info("[AccountJournal:addFundsToAccount]: {}", balance);
			
			return  BalanceBuilder.toResponse(balance);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new InvalidParameterException(
					"Couldn't transfer funds to account " + request.getAccountId());
		}
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

	@SneakyThrows
	public boolean verifyAccountHasSufficientFunds(Long accountId, Double amountRequired){

		Balance lastMovement = getLastMovementFromAccount(accountId);

		if (amountRequired > lastMovement.getBalance()) {
			log.error("[AccountJournal:verifyAccountHasSufficientFunds] Insufficient funds on account " + accountId);
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
