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
import team.nautilus.poc.concurrency.persistence.model.Balance;
import team.nautilus.poc.concurrency.persistence.model.constant.TransactionType;
import team.nautilus.poc.concurrency.persistence.repository.BalanceRepository;
import team.nautilus.poc.concurrency.service.AccountJournal;
import team.nautilus.poc.concurrency.service.AccountJournalCycling;
import team.nautilus.poc.concurrency.service.AccountJournalOptimistic;

@Slf4j
@Service
public class AccountJournalCyclingImpl extends AccountJournal implements AccountJournalCycling {

	public AccountJournalCyclingImpl(BalanceRepository repository) {
		super(repository);
		// TODO Auto-generated constructor stub
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
					.amount(request.getAmount())
					.balance(lastMovement.getBalance() - request.getAmount())
					.accountId(lastMovement.getAccountId())
					.timestamp(Instant.now())
					.type(TransactionType.TOP)
					.version(lastMovement.getVersion() + 1)
					.build();
			
			Long currentVersion = null;
			Integer counter = 1;
			
			while (lastMovement.getVersion() 
					!= (currentVersion = getRepository().getCurrentBalanceVersionByAccountId(lastMovement.getAccountId()))) {
				
				counter++;
				
				/*
				 * if we reach 3 dirty reading, rollback transaction v   b            
				 */
				if(counter > 3) {
					throw new ConcurrentModificationException("Dirty reading reach max limit: 3. Transaction will be rejected");
				}
				
				log.info("[AccountJournal: debitMovement] "
						+ "not same versions: {} - {}. "
						+ "Waiting for other updates to "
						+ "finish...",
						lastMovement.getVersion(), 
						currentVersion);
				
				/*
				 * wait a bit
				 */
				
//				Thread.sleep(350);
				
				lastMovement = getLastMovementFromAccount(request.getAccountId());
				debitMovement = Balance.builder()
						.accountId(lastMovement.getAccountId())
						.amount(request.getAmount())
						.balance(lastMovement.getBalance() - request.getAmount())
						.accountId(lastMovement.getAccountId())
						.timestamp(Instant.now())
						.version(lastMovement.getVersion() + 1)
						.build();
			}
			
			log.info("[AccountJournal: debitMovement] save new movement for transfer {}",
					request.getTransferReferenceId());
			
			log.info("[AccountJournal: debitMovement] version: {}, {}",
					lastMovement.getVersion(), 
					currentVersion);
			Balance balance = getRepository().save(debitMovement);
			log.info("[AccountJournal: debitMovement] new-version: {}, last-version: {}, balance: {}",
					balance.getVersion(), 
					currentVersion,
					balance.getBalance());
			
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