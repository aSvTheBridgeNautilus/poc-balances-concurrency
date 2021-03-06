package team.nautilus.poc.concurrency.persistence.model;

import java.io.Serializable;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import team.nautilus.poc.concurrency.persistence.model.converter.Instant2TimestampConverter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "BillingPeriod")
@IdClass(BillingPeriod.class)
public class BillingPeriod implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7255842483255740629L;

//	@Id
//	@SequenceGenerator(name = "billingPeriodIdSeqGen", sequenceName = "billingPeriodIdSeqGen", allocationSize = 1, initialValue = 1000)
//	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "billingPeriodIdSeqGen")
//	private Long id;
	
	@Id
	@JsonProperty(value = "account_id")
	@Column(name = "account_id", nullable = false, unique = false)
	private Long accountId;
	
	@Id
	@JsonProperty(value = "user_id")
	@Column(name = "user_id", nullable = false, unique = false)
	private String userId;
	
	@Id
	@JsonProperty("timestamp_utc")
	@Column(name = "timestamp_utc")
	@Convert(converter = Instant2TimestampConverter.class)
	private Instant timestamp;

//	@Id
//	@JsonProperty(value = "billing_date")
//	@Convert(converter = LocalDate2DateConverter.class)
//	@Column(name = "billing_date", columnDefinition = "date", nullable = false, unique = false)
//	private LocalDate billingDate;

	@JsonProperty(value = "billing_day")
	@Column(name = "billing_day", columnDefinition = "decimal(2, 0)", nullable = false)
	private Integer billingDay;

	@JsonProperty(value = "billing_cycle")
	@Column(name = "billing_cycle", columnDefinition = "decimal(3, 0) default 30", nullable = false)
	private Integer billingCycle;

	@JsonProperty(value = "count")
	@Column(name = "count", columnDefinition = "decimal(11, 0) default 0", nullable = false)
	private Long count;

	@JsonProperty(value = "transactions_cycle")
	@Column(name = "transactions_cycle", columnDefinition = "decimal(11, 0) default 100", nullable = false)
	private Long transactionsCycle;

	@Column(name = "balance", columnDefinition = "decimal(11, 2) default 0", nullable = false)
	private Double balance;


}
