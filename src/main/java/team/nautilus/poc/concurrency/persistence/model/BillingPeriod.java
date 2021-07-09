package team.nautilus.poc.concurrency.persistence.model;

import java.io.Serializable;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Index;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import team.nautilus.poc.concurrency.persistence.model.converter.Instant2TimestampConverter;
import team.nautilus.poc.concurrency.persistence.model.embeddable.BillingPeriodId;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "BillingPeriod", indexes = {
		@Index(name = "account_idx", columnList = "account_id"),
		@Index(name = "account_movement_idx", columnList = "account_id, movement_id"),
})
public class BillingPeriod implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7255842483255740629L;
	
	@EmbeddedId
	private BillingPeriodId id;
	
//	@JsonProperty(value = "account_id")
//	@Column(name = "account_id", insertable = false, updatable = false)
//	private Long accountId;
	
//	@JsonProperty(value = "user_id")
//	@Column(name = "user_id", insertable = false, updatable = false)
//	private String userId;
	
	@JsonProperty("timestamp_utc")
//	@Column(name = "timestamp_utc", columnDefinition = "timestamp")
//	@Convert(converter = LocalDate2DateConverter.class)
	private transient Instant timestamp;

//	@JsonProperty("movement_id")
//	@Column(name = "movement_id", insertable = false, updatable = false)
//	private Long movementId;

//	@JsonProperty(value = "billing_day")
//	@Column(name = "billing_day", columnDefinition = "decimal(2, 0)", nullable = false)
//	private Integer billingDay;

//	@JsonProperty(value = "billing_cycle")
//	@Column(name = "billing_cycle", columnDefinition = "decimal(3, 0) default 30", nullable = false)
//	private Integer billingCycle;

//	@JsonProperty(value = "transactions_count")
//	@Column(name = "transactions_count", columnDefinition = "decimal(11, 0) default 0", nullable = false)
//	private Long transactionsCount;

	@JsonProperty(value = "transactions_cycle")
	@Column(name = "transactions_cycle", columnDefinition = "decimal(11, 0) default 100", nullable = false)
	private Long transactionsCycle;

	@Column(name = "balance", columnDefinition = "decimal(11, 2) default 0", nullable = false)
	private Double balance;

	transient private Double cacheBalance;

}
