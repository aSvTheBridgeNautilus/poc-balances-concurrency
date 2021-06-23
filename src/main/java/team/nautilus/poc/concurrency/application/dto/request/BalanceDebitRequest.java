package team.nautilus.poc.concurrency.application.dto.request;

import java.io.Serializable;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceDebitRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6352553788705374888L;

	@Valid
	@NotNull(message = "account_id is required")
	@JsonProperty("account_id")
	private Long accountId;

	@Valid
	@NotNull(message = "user_id is required")
	@NotBlank(message = "user_id cannot be empty")
	@JsonProperty("user_id")
	private String userId;

	@Valid
	@NotNull(message = "currency is required")
	@NotBlank(message = "currency cannot be empty")
	private String currency;

	@Valid
	@NotNull(message = "amount is required")
	private Double amount;

	private String comments;

	@Valid
	@NotNull(message = "transfer_reference_id is required")
	@JsonProperty("transfer_reference_id")
	private Long transferReferenceId;

	@Valid
	@NotNull(message = "bank_reference_id is required")
	@NotBlank(message = "bank_reference_id cannot be empty")
	@JsonProperty("bank_reference_id")
	private String bankReferenceId;

}
