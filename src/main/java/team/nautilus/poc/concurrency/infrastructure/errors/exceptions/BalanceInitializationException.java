package team.nautilus.poc.concurrency.infrastructure.errors.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class BalanceInitializationException extends RuntimeException {

	public BalanceInitializationException() {
		super();
	}

	public BalanceInitializationException(String message) {
		super(message);
	}

}