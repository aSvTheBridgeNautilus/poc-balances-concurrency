package team.nautilus.poc.concurrency.persistence.repository.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import team.nautilus.poc.concurrency.persistence.repository.model.constant.TransactionType;
import team.nautilus.poc.concurrency.persistence.repository.model.converter.Instant2TimestampConverter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "poc_balance")
public class Balance implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7017015847594894320L;

	@Id
	@SequenceGenerator(name = "poc_idSeqGen", sequenceName = "poc_idSeqGen", allocationSize = 1)
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
	@Column(name = "timestamp_utc")
	@Convert(converter = Instant2TimestampConverter.class)
	private Instant timestamp;

	@JsonProperty("account_id")
	@Column(name = "account_id", nullable = false)
	@NotNull
	private Long accountId;
	
	@JsonProperty("type")
	@NotNull
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "type", nullable = false, columnDefinition = "decimal (1, 0) default 0")
	private TransactionType type;

	@Version
	@Column(columnDefinition = "bigint default 0", nullable = false)
	private Long version;

	@Override
	public int hashCode() {
		return Objects.hash(id, version);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Balance)) {
			return false;
		}
		Balance other = (Balance) obj;
		return Objects.equals(id, other.id) && Objects.equals(version, other.version);
	}


}
