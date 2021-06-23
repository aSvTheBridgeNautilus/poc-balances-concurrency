package team.nautilus.poc.concurrency.infrastructure.errors;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {

	@JsonProperty("timestamp")
	private Instant timestamp;

	@JsonProperty("message")
	private List<String> message;

	@JsonProperty("details")
	private String details;

	public ErrorResponse(Instant timestamp, String message, String details) {
		this(timestamp, List.of(message), details);
	}

}