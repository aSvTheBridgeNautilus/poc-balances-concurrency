package team.nautilus.poc.concurrency.infrastructure.errors;

import java.security.InvalidParameterException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.List;

import javax.naming.OperationNotSupportedException;
import javax.persistence.EntityNotFoundException;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import lombok.extern.slf4j.Slf4j;
import team.nautilus.poc.concurrency.infrastructure.errors.exceptions.AsyncCalculationException;
import team.nautilus.poc.concurrency.infrastructure.errors.exceptions.BalanceInitializationException;
import team.nautilus.poc.concurrency.infrastructure.errors.exceptions.ProcessNewBillingCycleException;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

	@ExceptionHandler(EntityNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	protected ResponseEntity<Object> handleResourceNotFound(EntityNotFoundException ex, WebRequest request) {
		ErrorResponse errorResponse = new ErrorResponse(Instant.now(), ex.getMessage(), request.getDescription(false));
		return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler({
		InvalidParameterException.class,
		ConcurrentModificationException.class,
		BalanceInitializationException.class,
		AsyncCalculationException.class,
		ProcessNewBillingCycleException.class,
		})
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	protected ResponseEntity<Object> handleInvalidParameter(Exception ex, WebRequest request) {
		ErrorResponse errorResponse = new ErrorResponse(Instant.now(), ex.getMessage(), request.getDescription(false));
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}
	
	/*
	 * Handler for annotation @Valid in
	 * single @RequestParam(required = false).
	 */
	@ExceptionHandler(ConstraintViolationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	protected ResponseEntity<Object> handleConstraintViolationException(Exception ex, WebRequest request) {
		ErrorResponse errorResponse = new ErrorResponse(
				Instant.now(), 
				ex.getMessage().substring(ex.getMessage().indexOf(":") + 2), 
				request.getDescription(false));
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex,
			WebRequest request) {
		List<String> errors = new ArrayList<>();
		
		ex.getAllErrors().forEach((error) -> {
			errors.add(error.getDefaultMessage());
		});
		
		ErrorResponse errorResponse = new ErrorResponse(Instant.now(), errors, request.getDescription(false));
		
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(errorResponse);
	}
	
	@ExceptionHandler(MissingServletRequestParameterException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<Object> handleMissingServletRequestParameterException(
			MissingServletRequestParameterException ex,
			WebRequest request) {

		List<String> errors = Arrays.asList(ex.getMessage());

		ErrorResponse errorResponse = new ErrorResponse(Instant.now(), errors, request.getDescription(false));

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						     .body(errorResponse);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	protected ResponseEntity<Object> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, WebRequest request) {
		log.error("handleOther", ex);
		ErrorResponse errorResponse = new ErrorResponse(Instant.now(), "JSON parse error or unexpected character", request.getDescription(false));
		
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	protected ResponseEntity<Object> handleOther(Exception ex, WebRequest request) {
		log.error("handleOther", ex);
		ErrorResponse errorResponse = new ErrorResponse(Instant.now(), ex.getMessage(), request.getDescription(false));

		return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}