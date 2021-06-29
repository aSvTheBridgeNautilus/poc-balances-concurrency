package team.nautilus.poc.concurrency.persistence.model.embeddable;

import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import team.nautilus.poc.concurrency.persistence.model.converter.LocalDate2DateConverter;

@Getter
@Setter
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class BillingPeriodId implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8404424036562665178L;

	@JsonProperty(value = "account_id")
	@Column(name = "account_id", nullable = false, unique = false)
	private Long accountId;

	@JsonProperty(value = "user_id")
	@Column(name = "user_id", nullable = false, unique = false)
	private String userId;

	@JsonProperty(value = "billing_date")
	@Convert(converter = LocalDate2DateConverter.class)
	@Column(name = "billing_date", columnDefinition = "date", nullable = false, unique = false)
	private LocalDate billingDate;

}
