package team.nautilus.poc.concurrency.application.controller;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import team.nautilus.poc.concurrency.application.dto.request.BalanceCreditRequest;
import team.nautilus.poc.concurrency.application.dto.request.BalanceDebitRequest;
import team.nautilus.poc.concurrency.application.dto.response.BalanceResponse;
import team.nautilus.poc.concurrency.service.AccountJournal;
import team.nautilus.poc.concurrency.service.AccountJournalOptimistic;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/poc/concurrency/optimistic")
public class BalanceOptimisticController {
	
	private final AccountJournalOptimistic journal;
	
	@SneakyThrows
	@PostMapping("/balance_debit")
	public ResponseEntity<BalanceResponse> balanceDebit(
			@Valid @RequestBody BalanceDebitRequest debitRequest) {
		log.debug("[BalanceController:balanceDebit] started...");

		return ResponseEntity.ok(journal.takeFundsFromAccount(debitRequest));
	}
	
	@PostMapping("/balance_credit")
	public ResponseEntity<BalanceResponse> balanceCredit(
			@Valid  @RequestBody BalanceCreditRequest creditRequest) {
		log.debug("[BalanceController:balanceCredit] started...");
		
		return ResponseEntity.ok(journal.addFundsToAccount(creditRequest));
	}	

}
