package team.nautilus.poc.concurrency.infrastructure.errors.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class TopUpTransferException extends RuntimeException {

	public TopUpTransferException() {
		super();
	}

	public TopUpTransferException(String message) {
		super(message);
	}

}