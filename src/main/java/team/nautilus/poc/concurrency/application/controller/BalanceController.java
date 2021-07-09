package team.nautilus.poc.concurrency.application.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.ArrayList;
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
import team.nautilus.poc.concurrency.application.dto.request.BalanceTransferRequest;
import team.nautilus.poc.concurrency.application.dto.response.BalanceResponse;
import team.nautilus.poc.concurrency.application.dto.response.BillingPeriodPOCResponse;
import team.nautilus.poc.concurrency.application.dto.response.BillingPeriodReportResponse;
import team.nautilus.poc.concurrency.application.dto.response.TransferResponse;
import team.nautilus.poc.concurrency.application.facade.AccountJournalFacade;
import team.nautilus.poc.concurrency.persistence.model.Balance;
import team.nautilus.poc.concurrency.persistence.model.BillingPeriod;
import team.nautilus.poc.concurrency.persistence.repository.BalanceRepository;
import team.nautilus.poc.concurrency.persistence.repository.BillingPeriodRepository;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/poc/concurrency/billing_period")
public class BalanceController {

	private final BillingPeriodRepository billingRepository;
	private final BalanceRepository repository;
	private final AccountJournalFacade journalFacade;

	@SneakyThrows
	@GetMapping("/balances/billing_report")
	public @ResponseBody ResponseEntity<BillingPeriodReportResponse> getBillingReport(
			@Valid 
			@NotNull(message = "account_id cannot be empty") 
			@RequestParam(value = "account_id", required = false) 
			Long accountId) {
		
		log.debug("[BalanceController:getBalance] started");
		
		
		List<BillingPeriodPOCResponse> periodsReport = new ArrayList<>();
		
		List<BillingPeriod> periods = billingRepository.getAllBillingPeriodsByAccountId(accountId);
		
		int index = 0;
		Instant fromMIN = LocalDate.of(2020, Month.JANUARY, 1).atStartOfDay().atOffset(ZoneOffset.UTC).toInstant(); 
		for(BillingPeriod period : periods) {
			List<Object[]> sumAndCount = (List<Object[]>) 
					repository.getBillingPeriodBalanceByAccountId(
					accountId, 
					index == 0 
					? -1
					: periods.get(index - 1).getId().getMovementId(),
					period.getId().getMovementId());
			
					Double sum = null;
					Long count = null;
					Double realBalance = repository.getBalanceUntilBillingPeriod(
							accountId, 
							period.getId().getMovementId());
					Double diff = null;
					
			try {
				count = ((Number)sumAndCount.get(0)[1]).longValue();
				
				sum = count == 0l ? 0d : ((Number)sumAndCount.get(0)[0]).doubleValue();
				
				diff = realBalance - sum;
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			BillingPeriodPOCResponse response =
					BillingPeriodPOCResponse
					.builder()
					.accountId(accountId)
//					.fromTimestamp(index == 0 ? null : periods.get(index - 1).getTimestamp())
					.fromId(index == 0 ? -1l : periods.get(index - 1).getId().getMovementId())
//					.toTimestamp(period.getTimestamp())
					.toId(period.getId().getMovementId())
					.periodBalance(period.getBalance())
					.realBalance(realBalance)
					.count(count)
					.sum(sum)
					.difference(diff)
					.build();
			
			periodsReport.add(response);
			index++;
		}
		
		return ResponseEntity.ok(BillingPeriodReportResponse
										.builder()
										.total(periodsReport.size())
										.periods(periodsReport)
										.build());
	}
	
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
	@GetMapping("/balances/complete")
	public @ResponseBody ResponseEntity<BalanceResponse> getCompleteBalance(
			@Valid 
			@NotNull(message = "account_id cannot be empty") 
			@RequestParam(value = "account_id", required = false) 
			Long accountId) {

		log.debug("[BalanceController:getBalance] started");
		return ResponseEntity.ok(
				BalanceResponse
				.builder()
				.accountId(accountId)
				.amount(repository.getCompleteBillingPeriodBalanceByAccountId(accountId))
				.build()
				);
	}

	@SneakyThrows
	@PostMapping("/balance_init")
	public @ResponseBody ResponseEntity<BalanceResponse> initBalance(
			@Valid  
			@RequestBody 
			BalanceInitializationRequest initRequest) {
		log.debug("[BalanceController:initBalance] started...");

		return ResponseEntity.ok(journalFacade.initializeBalance(initRequest));
	}
	
	@SneakyThrows
	@PostMapping("/balance_debit")
	public @ResponseBody ResponseEntity<BalanceResponse> balanceDebit(
			@Valid @RequestBody BalanceDebitRequest debitRequest) {
		log.debug("[BalanceController:balanceDebit] started...");

		return ResponseEntity.ok(journalFacade.takeFundsFromAccount(debitRequest));
	}
	
	@PostMapping("/balance_credit")
	public @ResponseBody ResponseEntity<BalanceResponse> balanceCredit(
			@Valid  
			@RequestBody 
			BalanceCreditRequest creditRequest) {
		log.debug("[BalanceController:balanceCredit] started...");
		
		return ResponseEntity.ok(journalFacade.addFundsToAccount(creditRequest));
	}	
	
	@SneakyThrows
	@PostMapping("/balance_transfer")
	public @ResponseBody ResponseEntity<TransferResponse> transferBalance(
			@Valid 
			@RequestBody 
			BalanceTransferRequest transferRequest) {
		log.debug("[BalanceController:createBalanceTransfer] started...");

		return ResponseEntity.ok(journalFacade.registerTransfer(transferRequest));
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
