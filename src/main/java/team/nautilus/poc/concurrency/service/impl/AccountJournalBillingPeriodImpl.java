package team.nautilus.poc.concurrency.service.impl;

import java.security.InvalidParameterException;
import java.time.Instant;
import java.util.ConcurrentModificationException;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import team.nautilus.poc.concurrency.application.dto.builder.BalanceBuilder;
import team.nautilus.poc.concurrency.application.dto.request.BalanceCreditRequest;
import team.nautilus.poc.concurrency.application.dto.request.BalanceDebitRequest;
import team.nautilus.poc.concurrency.application.dto.response.BalanceResponse;
import team.nautilus.poc.concurrency.infrastructure.errors.exceptions.InsufficientFundsException;
import team.nautilus.poc.concurrency.persistence.model.Balance;
import team.nautilus.poc.concurrency.persistence.model.constant.TransactionType;
import team.nautilus.poc.concurrency.persistence.repository.BalanceRepository;
import team.nautilus.poc.concurrency.service.AccountJournal;
import team.nautilus.poc.concurrency.service.AccountJournalBillingPeriod;
import team.nautilus.poc.concurrency.service.AccountJournalOptimistic;

@Slf4j
@Service
public class AccountJournalBillingPeriodImpl extends AccountJournal implements AccountJournalBillingPeriod {

	public AccountJournalBillingPeriodImpl(BalanceRepository repository) {
		super(repository);
		// TODO Auto-generated constructor stub
	}
	
	
	@SneakyThrows
	public boolean verifyAccountHasSufficientFunds(Long accountId, Double amountRequired){
		if (amountRequired > getCurrentBillingPeriodBalanceByAccountId(accountId)) {
			log.error("[AccountJournal:verifyAccountHasSufficientFunds] Insufficient funds on account " + accountId);
			throw new InsufficientFundsException("Insufficient funds on account " + accountId);
		}

		return true;
	}


	@Override
	@SneakyThrows
	@Transactional(rollbackFor = RuntimeException.class)
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
					.amount(-request.getAmount())
					.balance(getCurrentBillingPeriodBalanceByAccountId(request.getAccountId()) - request.getAmount())
					.accountId(lastMovement.getAccountId())
					.timestamp(Instant.now())
					.type(TransactionType.TOP)
					.version(lastMovement.getVersion() + 1)
					.build();
			


			Balance balance = getRepository().save(debitMovement);

			
			log.info("[AccountJournal: debitMovement] {}", balance);
			
			return  BalanceBuilder.toResponse(balance);
		} catch (Exception ex) {
			throw new InvalidParameterException("Couldn't take funds from account " + request.getAccountId());
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
					.balance(getCurrentBillingPeriodBalanceByAccountId(request.getAccountId()) + request.getAmount())
					.accountId(lastMovement.getAccountId())
					.timestamp(Instant.now())
					.type(TransactionType.TOP)
					.build();
			
			Balance balance = getRepository().save(creditMovement);
			
			log.info("[AccountJournal: addFundsToAccountFrom]: {}", balance);
			
			return  BalanceBuilder.toResponse(balance);
		} catch (Exception ex) {
			throw new InvalidParameterException(
					"Couldn't transfer funds to account " + request.getAccountId());
		}
	}


}
