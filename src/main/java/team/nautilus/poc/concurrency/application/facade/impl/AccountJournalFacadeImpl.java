package team.nautilus.poc.concurrency.application.facade.impl;

import java.security.InvalidParameterException;
import java.time.Instant;
import java.util.ConcurrentModificationException;
import java.util.Objects;

import javax.validation.Valid;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import team.nautilus.poc.concurrency.application.dto.builder.BalanceBuilder;
import team.nautilus.poc.concurrency.application.dto.request.BalanceCreditRequest;
import team.nautilus.poc.concurrency.application.dto.request.BalanceDebitRequest;
import team.nautilus.poc.concurrency.application.dto.request.BalanceInitializationRequest;
import team.nautilus.poc.concurrency.application.dto.request.BalanceTransferRequest;
import team.nautilus.poc.concurrency.application.dto.response.BalanceResponse;
import team.nautilus.poc.concurrency.application.dto.response.TransferResponse;
import team.nautilus.poc.concurrency.application.facade.AccountJournalFacade;
import team.nautilus.poc.concurrency.infrastructure.errors.exceptions.BalanceInitializationException;
import team.nautilus.poc.concurrency.infrastructure.errors.exceptions.InsufficientFundsException;
import team.nautilus.poc.concurrency.infrastructure.errors.exceptions.ProcessNewBillingCycleException;
import team.nautilus.poc.concurrency.persistence.model.Balance;
import team.nautilus.poc.concurrency.persistence.model.constant.OperationType;
import team.nautilus.poc.concurrency.persistence.model.embeddables.BalanceMovement;
import team.nautilus.poc.concurrency.persistence.repository.BalanceRepository;
import team.nautilus.poc.concurrency.service.AccountJournal;
import team.nautilus.poc.concurrency.service.BillingPeriodService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountJournalFacadeImpl implements AccountJournalFacade {

	private final BalanceRepository balanceRepository;
	private final AccountJournal journalService;
	private final BillingPeriodService billingService;
	
	@Override
	@SneakyThrows
	public Double verifyAccountHasSufficientFunds(Balance lastMovement, Double amountRequired) {
		Long accountId = lastMovement.getAccountId();
		Double periodBalance = null;
		if (amountRequired > (periodBalance = billingService.getCurrenBillingPeriodBalanceFromAccount(lastMovement))) {
			log.error("[AccountJournal:verifyAccountHasSufficientFunds] Insufficient funds on account " + accountId);
			throw new InsufficientFundsException("Insufficient funds on account " + accountId);
		}

		return periodBalance;
	}
	
	@SneakyThrows
	@Transactional(rollbackFor = RuntimeException.class)
	public BalanceResponse takeFundsFromAccount(BalanceDebitRequest request) {
		log.debug("[AccountJournalFacade:takeFundsFromAccount] started...");
		
		try {
			
			Balance lastMovement = journalService.getLastMovementFromAccount(request.getAccountId());
			Long accountId = lastMovement.getAccountId();
			verifyAccountHasSufficientFunds(lastMovement, request.getAmount());
			Balance debitMovement =  Balance.builder()
					.accountId(lastMovement.getAccountId())
					.amount(-request.getAmount())
					.balance(billingService.getCurrenBillingPeriodBalanceFromAccount(lastMovement) - request.getAmount())
					.accountId(lastMovement.getAccountId())
					.timestamp(Instant.now())
					.operationType(OperationType.DEBIT)
					.movement(BalanceMovement
							.builder()
							.id(journalService.generateNewMovementIdForAccount(accountId))
							.build())
					.build();
			
			
			log.info("[AccountJournal:takeFundsFromAccount] save new movement of Account {}",
					lastMovement.getAccountId());
			
			Balance balance = balanceRepository.save(debitMovement);
		
			log.info("[AccountJournal:takeFundsFromAccount] {}", balance);
			
			return  BalanceBuilder.toResponse(balance);
		} catch (InsufficientFundsException ex) {
			ex.printStackTrace();
			throw ex;
		} catch (ConcurrentModificationException ex) {
			ex.printStackTrace();
			throw ex;
		} catch (ProcessNewBillingCycleException ex) {
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new InvalidParameterException("Couldn't take funds from account " + request.getAccountId());
		}
	}
	
	@SneakyThrows
	@Transactional(rollbackFor = Exception.class)
	public BalanceResponse addFundsToAccount(BalanceCreditRequest request) {
		try {
			
			Balance lastMovement = journalService.getLastMovementFromAccount(request.getAccountId());
			Long accountId = lastMovement.getAccountId();
			Balance creditMovement =  Balance.builder()
					.accountId(lastMovement.getAccountId())
					.amount(request.getAmount())
					.balance(billingService.getCurrenBillingPeriodBalanceFromAccount(lastMovement) + request.getAmount())
					.accountId(lastMovement.getAccountId())
					.timestamp(Instant.now())
					.operationType(OperationType.CREDIT)
					.movement(BalanceMovement
							.builder()
							.id(journalService.generateNewMovementIdForAccount(accountId))
							.build())
					.build();

			
			log.info("[AccountJournal:addFundsToAccount] save new movement of Account {}",
					lastMovement.getAccountId());
			
			Balance balance = balanceRepository.save(creditMovement);

			log.info("[AccountJournal:addFundsToAccount]: {}", balance);
			
			return  BalanceBuilder.toResponse(balance);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new InvalidParameterException(
					"Couldn't transfer funds to account " + request.getAccountId());
		}
	}
	
	@Override
	@SneakyThrows
	@Transactional(rollbackFor = RuntimeException.class)
	public BalanceResponse initializeBalance(BalanceInitializationRequest request) {
		try {
			// save initial balance
			balanceRepository.save(Balance
					    .builder()
					    .accountId(request.getAccountId())
					    .amount(0d)
					    .balance(0d)
					    .timestamp(Instant.now())
					    .operationType(OperationType.INITIAL)
					    .movement(BalanceMovement
					    		.builder()
					    		.id(0l)
					    		.build())
						.build());
			/*
			 * implement from here down
			 */
			billingService.createFirstBillingPeriodForAccount(request);
			log.info("[AccountJournal:initializeBalance] First billing period of account {} initialized", request.getAccountId());
			return BalanceResponse
						.builder()
					    .accountId(request.getAccountId())
						.amount(0d)
						.build();
			
		} catch (BalanceInitializationException ex) {
			log.error("[AccountJournal:initializeBalance] account {} already initialized", request.getAccountId());
			throw ex;
		} catch (RuntimeException ex) {
			throw new BalanceInitializationException("Initialization request invalid for account " + request.getAccountId());
		}
	}

	@Override
	@SneakyThrows
	public BalanceResponse getBalanceFromCurrentBillingPeriodOfAccount(Long accountId) {
		log.info("[AccountJournal:getBalanceFromCurrentBillingPeriodOfAccount] Calculating balance of current Billing Period for account{}", accountId);
		
		// get last movement form account
		Balance latestMovement = journalService.getLastMovementFromAccount(accountId);
		return BalanceBuilder.toCurrentBillingPeriodBalanceResponse(
						latestMovement,
						billingService.getCurrenBillingPeriodBalanceFromAccount(latestMovement));
	}

	@Override
	public TransferResponse registerTransfer(@Valid BalanceTransferRequest request) {
		log.debug("[AccountJournalFacade:registerTransfer] started");
		Balance sourceAccount = journalService.getLastMovementFromAccount(request.getSourceAccountId());
		
		Double sourcePeriodBalance = verifyAccountHasSufficientFunds(sourceAccount, request.getAmount());
		
		Balance targetAccount = journalService.getLastMovementFromAccount(request.getTargetAccountId());

		Instant timestamp = Instant.now(); // always in UTC

		Balance sourceMov = BalanceBuilder.toNewMovement(
				sourceAccount, 
				sourcePeriodBalance, 
				journalService.generateNewMovementIdForAccount(sourceAccount.getAccountId()), 
				request, 
				timestamp, OperationType.DEBIT);
		Balance targetMov = BalanceBuilder.toNewMovement(
				targetAccount, 
				billingService.getCurrenBillingPeriodBalanceFromAccount(targetAccount),
				journalService.generateNewMovementIdForAccount(targetAccount.getAccountId()), 
				request, 
				timestamp, OperationType.CREDIT);

		boolean result = journalService.persistMovementsInSingleTransaction(sourceMov, targetMov);
		return TransferResponse
				   .builder()
				   .result(Boolean.TRUE.equals(result) ? "OK" : "Transfer failed")
				   .build();
	}
	
}
