package team.nautilus.poc.concurrency.persistence.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import team.nautilus.poc.concurrency.persistence.model.constant.OperationType;
import team.nautilus.poc.concurrency.persistence.model.converter.Instant2TimestampConverter;
import team.nautilus.poc.concurrency.persistence.model.embeddables.BalanceMovement;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "poc_balance", indexes = {
		@Index(name = "billing_period_search_idx", columnList = "id, account_id"),
})
public class Balance implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7017015847594894320L;

	@Id
	@SequenceGenerator(name = "poc_idSeqGen", sequenceName = "poc_idSeqGen", allocationSize = 1, initialValue = 100)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "poc_idSeqGen")
	@Column(nullable = false, unique = true)
	private Long id;

	@Column(columnDefinition = "decimal(11, 2) default 0", nullable = false)
	private Double amount = 0d;
	 
	@NotNull
	@Column(name = "balance", columnDefinition = "decimal(11, 2) default 0", nullable = false)
	private Double balance;

	@JsonProperty("timestamp_utc")
	@NotNull
	@Column(name = "timestamp_utc", columnDefinition = "timestamp")
	@Convert(converter = Instant2TimestampConverter.class)
	private Instant timestamp;

	@JsonProperty("account_id")
	@Column(name = "account_id", nullable = false)
	@NotNull
	private Long accountId;
	
	@JsonProperty("operation_type")
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "operation_type", columnDefinition = "decimal (1, 0) default 1")
	private OperationType operationType;

	@Embedded
	@AttributeOverrides({ 
		@AttributeOverride(name = "id", column = @Column(name = "movement_id", nullable = false))
		})
	private BalanceMovement movement;

	@Override
	public int hashCode() {
		return Objects.hash(accountId, id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Balance))
			return false;
		Balance other = (Balance) obj;
		return Objects.equals(accountId, other.accountId) && Objects.equals(id, other.id);
	}

}
