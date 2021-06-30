package team.nautilus.poc.concurrency.infrastructure.errors.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class AsyncCalculationException extends RuntimeException {

	public AsyncCalculationException() {
		super();
	}

	public AsyncCalculationException(String message) {
		super(message);
	}

}