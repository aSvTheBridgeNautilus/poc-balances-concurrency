package team.nautilus.poc.concurrency.application.controller;

import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import team.nautilus.poc.concurrency.application.dto.request.BalanceCreditRequest;
import team.nautilus.poc.concurrency.application.dto.request.BalanceDebitRequest;
import team.nautilus.poc.concurrency.application.dto.request.BalanceInitializationRequest;
import team.nautilus.poc.concurrency.application.dto.response.BalanceResponse;
import team.nautilus.poc.concurrency.application.facade.AccountJournalFacade;
import team.nautilus.poc.concurrency.persistence.model.Balance;
import team.nautilus.poc.concurrency.persistence.repository.BalanceRepository;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/poc/concurrency/billing_period")
public class BalanceController {

	private final BalanceRepository repository;
	private final AccountJournalFacade journalFacade;

	@SneakyThrows
	@GetMapping("/balances")
	public @ResponseBody ResponseEntity<BalanceResponse> getBalance(
			@Valid 
			@NotNull(message = "account_id cannot be empty") 
			@RequestParam(value = "account_id", required = false) 
			Long accountId) {

		log.debug("[BalanceController:getBalance] started");
		return ResponseEntity.ok(journalFacade.getBalanceFromCurrentBillingPeriodOfAccount(accountId));
	}

	@SneakyThrows
	@PostMapping("/balance_init")
	public ResponseEntity<BalanceResponse> initBalance(
			@Valid  
			@RequestBody 
			BalanceInitializationRequest initRequest) {
		log.debug("[BalanceController:initBalance] started...");

		return ResponseEntity.ok(journalFacade.initializeBalance(initRequest));
	}
	
	@SneakyThrows
	@PostMapping("/balance_debit")
	public ResponseEntity<BalanceResponse> balanceDebit(
			@Valid @RequestBody BalanceDebitRequest debitRequest) {
		log.debug("[BalanceController:balanceDebit] started...");

		return ResponseEntity.ok(journalFacade.takeFundsFromAccount(debitRequest));
	}
	
	@PostMapping("/balance_credit")
	public ResponseEntity<BalanceResponse> balanceCredit(
			@Valid  
			@RequestBody 
			BalanceCreditRequest creditRequest) {
		log.debug("[BalanceController:balanceCredit] started...");
		
		return ResponseEntity.ok(journalFacade.addFundsToAccount(creditRequest));
	}	
	
	@SneakyThrows
	@GetMapping("/balances_full_details")
	public @ResponseBody ResponseEntity<Balance> getBalanceFullDetails(
			@Valid 
			@NotNull(message = "account_id cannot be empty") 
			@RequestParam(value = "account_id", required = false) 
			Long accountId) {
		
		log.debug("[BalanceController:getBalance] started");
		
		List<Balance> balances = repository.findAllByAccountId(accountId,
				PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "timestamp")));
		
		if (balances.isEmpty()) {
			throw new EntityNotFoundException("No movements found for account " + accountId);
		}
		
		return ResponseEntity.ok(balances.get(0));
	}

}
