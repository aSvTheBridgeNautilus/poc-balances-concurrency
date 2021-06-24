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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import team.nautilus.poc.concurrency.application.dto.builder.BalanceBuilder;
import team.nautilus.poc.concurrency.application.dto.response.BalanceResponse;
import team.nautilus.poc.concurrency.persistence.model.Balance;
import team.nautilus.poc.concurrency.persistence.repository.BalanceRepository;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/poc/concurrency")
public class BalanceGlobalController {

	private final BalanceRepository repository;

	@SneakyThrows
	@GetMapping("/balances")
	public @ResponseBody ResponseEntity<BalanceResponse> getBalance(
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

		return ResponseEntity.ok(BalanceBuilder.toCurrentBalanceResponse(balances.get(0)));
	}

}
