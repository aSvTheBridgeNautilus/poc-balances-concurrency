package team.nautilus.poc.concurrency.application.controller;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

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
import team.nautilus.poc.concurrency.application.dto.builder.BalanceBuilder;
import team.nautilus.poc.concurrency.application.dto.request.BalanceInitializationRequest;
import team.nautilus.poc.concurrency.application.dto.response.BalanceResponse;
import team.nautilus.poc.concurrency.application.facade.AccountJournalFacade;
import team.nautilus.poc.concurrency.persistence.model.Balance;
import team.nautilus.poc.concurrency.service.AccountJournalBillingPeriod;
import team.nautilus.poc.concurrency.service.BillingPeriodService;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/poc/concurrency/billing_period")
public class BalanceBillingPeriodController {

	private final AccountJournalBillingPeriod billingFacade;
	private final AccountJournalFacade journalFacade;
	private final BillingPeriodService billingPeriodService;

	@SneakyThrows
	@GetMapping("/balances")
	public @ResponseBody ResponseEntity<BalanceResponse> getBalance(
			@Valid @NotNull(message = "account_id cannot be empty") @RequestParam(value = "account_id", required = false) Long accountId) {

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

}
