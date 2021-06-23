/*
 * NAUTILUS PROJECT
 */

package team.nautilus.poc.concurrency.application.dto.response;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Antonio Salazar Valero Created on : Apr 7, 2021, 6:52:23 PM
 */

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceResponse {

	private Long id;

	private Double amount = 0d;

	private String currency;

	@JsonProperty("timestamp_utc")
	private Instant timestamp;

	@JsonProperty("account_id")
	private Long accountId;

	@JsonProperty("user_id")
	private String userId;

}
