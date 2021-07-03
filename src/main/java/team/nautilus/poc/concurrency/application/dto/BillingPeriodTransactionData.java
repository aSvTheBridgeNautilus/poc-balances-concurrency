package team.nautilus.poc.concurrency.application.dto;

import java.io.Serializable;

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
public class BillingPeriodTransactionData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4744267554682097841L;

	private Long count;

	private Double balance;

}
