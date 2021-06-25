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
import team.nautilus.poc.concurrency.service.AccountJournalOptimistic;

@Slf4j
@Service
public class AccountJournalOptimisticImpl extends AccountJournal implements AccountJournalOptimistic {

	public AccountJournalOptimisticImpl(BalanceRepository repository) {
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
					.amount(-request.getAmount())
					.balance(lastMovement.getBalance() - request.getAmount())
					.accountId(lastMovement.getAccountId())
					.timestamp(Instant.now())
					.type(TransactionType.TOP)
					.version(lastMovement.getVersion() + 1)
					.build();
			
			Long currentVersion = lastMovement.getVersion();
			log.debug("[AccountJournalFacade:takeFundsFromAccount] current version: {}", currentVersion);
			
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
				
				log.info("[AccountJournal:takeFundsFromAccount] "
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
						.amount(-request.getAmount())
						.balance(lastMovement.getBalance() - request.getAmount())
						.accountId(lastMovement.getAccountId())
						.timestamp(Instant.now())
						.version(lastMovement.getVersion() + 1)
						.build();
			}	
		
			
			
			
			log.info("[AccountJournal:takeFundsFromAccount] save new movement of Account {}",
					lastMovement.getAccountId());
			
			Balance balance = getRepository().save(debitMovement);
			log.info("[AccountJournal:takeFundsFromAccount] update version of Account {} to {} ",
					lastMovement.getAccountId(),
					lastMovement.getVersion() + 1);
			getRepository().updateMovementsVersionByAccountId(lastMovement.getAccountId(), lastMovement.getVersion() + 1);
			
			log.info("[AccountJournal:takeFundsFromAccount] {}", balance);
			
			return  BalanceBuilder.toResponse(balance);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new InvalidParameterException("Couldn't take funds from account " + request.getAccountId());
		}
		
	}
	
	@Override
	@SneakyThrows
	@Transactional(rollbackFor = Exception.class)
	public BalanceResponse addFundsToAccount(BalanceCreditRequest request) {
		try {
			
			Balance lastMovement = getLastMovementFromAccount(request.getAccountId());
			Balance creditMovement =  Balance.builder()
					.accountId(lastMovement.getAccountId())
					.amount(request.getAmount())
					.balance(lastMovement.getBalance() + request.getAmount())
					.accountId(lastMovement.getAccountId())
					.timestamp(Instant.now())
					.type(TransactionType.TOP)
					.version(lastMovement.getVersion() + 1)
					.build();
			
			Long currentVersion = lastMovement.getVersion();
			log.debug("[AccountJournalFacade:takeFundsFromAccount] current version: {}", currentVersion);
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
				
				log.info("[AccountJournal:addFundsToAccount] "
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
				creditMovement = Balance.builder()
						.accountId(lastMovement.getAccountId())
						.amount(request.getAmount())
						.balance(lastMovement.getBalance() + request.getAmount())
						.accountId(lastMovement.getAccountId())
						.timestamp(Instant.now())
						.version(lastMovement.getVersion() + 1)
						.build();
			}	
			
	
			
			log.info("[AccountJournal:addFundsToAccount] save new movement of Account {}",
					lastMovement.getAccountId());
			
			Balance balance = getRepository().save(creditMovement);
			log.info("[AccountJournal:addFundsToAccount] update version of Account {} to {} ",
					lastMovement.getAccountId(),
					lastMovement.getVersion() + 1);
			getRepository().updateMovementsVersionByAccountId(lastMovement.getAccountId(), lastMovement.getVersion() + 1);
			
			log.info("[AccountJournal:addFundsToAccount]: {}", balance);
			
			return  BalanceBuilder.toResponse(balance);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new InvalidParameterException(
					"Couldn't transfer funds to account " + request.getAccountId());
		}
	}


}
