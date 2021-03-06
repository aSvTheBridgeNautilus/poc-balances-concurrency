package team.nautilus.poc.concurrency.infrastructure.errors.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ProcessNewBillingCycleException extends RuntimeException {

	public ProcessNewBillingCycleException() {
		super();
	}

	public ProcessNewBillingCycleException(String message) {
		super(message);
	}

}