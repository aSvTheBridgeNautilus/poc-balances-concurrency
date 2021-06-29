package team.nautilus.poc.concurrency.application.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceInitializationRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6352553788705374888L;

	@Valid
	@NotNull(message = "account_id is required")
	@JsonProperty("account_id")
	private Long accountId;

	@Valid
	@NotNull(message = "account_id is required")
	@NotEmpty(message = "user_id cannot be empty")
	@JsonProperty("user_id")
	private String userId;

	@Valid
	@NotNull(message = "currency is required")
	@NotBlank(message = "currency cannot be empty")
	private String currency;

}
