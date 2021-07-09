/*
 * NAUTILUS PROJECT
 */

package team.nautilus.poc.concurrency.application.dto.response;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class BillingPeriodPOCResponse {

	@JsonProperty(value = "index")
	private Integer index;

	@JsonProperty(value = "account_id")
	private Long accountId;

	@JsonProperty("from_time")
	private Instant fromTimestamp;

	@JsonProperty("from_id")
	private Long fromId;

	@JsonProperty("to_time")
	private Instant toTimestamp;

	@JsonProperty("to_id")
	private Long toId;

	@JsonProperty("period_balance")
	private Double periodBalance;

	@JsonProperty("real_balance")
	private Double realBalance;

	@JsonProperty("trxs_count")
	private Long count;

	@JsonProperty("trxs_sum")
	private Double sum;

	@JsonProperty("diff")
	private Double difference;
}
