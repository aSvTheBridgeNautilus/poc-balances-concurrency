package team.nautilus.poc.concurrency.infrastructure.errors.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class BillingPeriodOutdatedException extends RuntimeException {

	public BillingPeriodOutdatedException() {
		super();
	}

	public BillingPeriodOutdatedException(String message) {
		super(message);
	}

}