package team.nautilus.poc.concurrency.application.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceTransferRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6352553788705374888L;

	@Valid
	@NotNull(message = "source_account_id is required")
	@JsonProperty("source_account_id")
	private Long sourceAccountId;

	@Valid
	@NotNull(message = "target_account_id is required")
	@JsonProperty("target_account_id")
	private Long targetAccountId;

	@Valid
	@NotNull(message = "transfer_reference_id is required")
	@JsonProperty("transfer_reference_id")
	private Long targetReferenceId;

	@Valid
	@NotNull(message = "amount is required")
	@DecimalMin(value = "0.01", inclusive = false, message = "amount cannot be zero")
	private Double amount = 0d;

	private String comments;

}
