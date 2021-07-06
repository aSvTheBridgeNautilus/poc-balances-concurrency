package team.nautilus.poc.concurrency.persistence.model.embeddables;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

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
@Embeddable
public class BalanceMovement implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2265445640307870674L;

	@JsonProperty("movement_id")
	@Column(name = "movement_id", nullable = false)
	private Long id;

	
}
