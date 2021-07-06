/*
 * NAUTILUS PROJECT
 */

package team.nautilus.poc.concurrency.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Antonio Salazar Valero Created on : Mar 26, 2021, 10:28:05 AM
 */

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {

	@JsonProperty("operation_result")
	private String result;

}
