package team.nautilus.poc.concurrency.application.dto.builder;

import lombok.extern.slf4j.Slf4j;
import team.nautilus.poc.concurrency.application.dto.response.BalanceResponse;
import team.nautilus.poc.concurrency.persistence.model.Balance;

@Slf4j
public class BalanceBuilder {

	public static BalanceResponse toResponse(Balance balance) {
		return BalanceResponse
				.builder()
				.id(balance.getId())
				.accountId(balance.getAccountId())
				.amount(balance.getAmount())
				.timestamp(balance.getTimestamp())
				.build();
	}

	public static BalanceResponse toCurrentBalanceResponse(Balance balance) {
		return BalanceResponse
				.builder()
				.id(balance.getId())
				.accountId(balance.getAccountId())
				.amount(balance.getBalance())
				.timestamp(balance.getTimestamp())
				.build();
	}

	public static BalanceResponse toLastcycleBalanceResponse(Balance balance, Double lastCycleBalance) {
		return BalanceResponse
				.builder()
				.id(balance.getId())
				.accountId(balance.getAccountId())
				.amount(lastCycleBalance)
				.timestamp(balance.getTimestamp())
				.build();
	}

}
