package team.nautilus.poc.concurrency.infrastructure.errors.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class CreateMovementsException extends RuntimeException {

	public CreateMovementsException() {
		super();
	}

	public CreateMovementsException(String message) {
		super(message);
	}

}