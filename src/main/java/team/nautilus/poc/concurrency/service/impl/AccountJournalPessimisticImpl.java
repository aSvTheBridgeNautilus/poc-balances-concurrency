package team.nautilus.poc.concurrency.service.impl;

import java.security.InvalidParameterException;
import java.time.Instant;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import team.nautilus.poc.concurrency.application.dto.builder.BalanceBuilder;
import team.nautilus.poc.concurrency.application.dto.request.BalanceCreditRequest;
import team.nautilus.poc.concurrency.application.dto.request.BalanceDebitRequest;
import team.nautilus.poc.concurrency.application.dto.response.BalanceResponse;
import team.nautilus.poc.concurrency.application.mapper.dto.LocalDate2InstantUTCMapper;
import team.nautilus.poc.concurrency.persistence.model.Balance;
import team.nautilus.poc.concurrency.persistence.model.constant.TransactionType;
import team.nautilus.poc.concurrency.persistence.repository.BalanceRepository;
import team.nautilus.poc.concurrency.service.AccountJournal;
import team.nautilus.poc.concurrency.service.AccountJournalPessimistic;
import team.nautilus.poc.concurrency.service.BillingPeriodService;

@Slf4j
@Service
public class AccountJournalPessimisticImpl extends AccountJournal implements AccountJournalPessimistic {

	public AccountJournalPessimisticImpl(BalanceRepository repository, LocalDate2InstantUTCMapper dateUTCMapper,
			BillingPeriodService billingPeriodService) {
		super(repository, dateUTCMapper, billingPeriodService);
		// TODO Auto-generated constructor stub
	}

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
					.type(TransactionType.TOP)
					.build();
			
			Long currentVersion = null;
			
			while (lastMovement.getVersion() 
					!= (currentVersion = getRepository().getCurrentBalanceVersionByAccountId(lastMovement.getAccountId()))) {
				
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
						.type(TransactionType.TOP)
						.build();
			}
			
			log.info("[AccountJournal: debitMovement] save new movement for transfer {}",
					request.getTransferReferenceId());
			
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
					.balance(lastMovement.getBalance() + request.getAmount())
					.accountId(lastMovement.getAccountId())
					.timestamp(Instant.now())
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
