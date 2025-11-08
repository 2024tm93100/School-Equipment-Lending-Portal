package com.school.lending.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	// Handler for DuplicateResourceException (HTTP 409 Conflict)
	@ExceptionHandler(DuplicateResourceException.class)
	public ProblemDetail handleDuplicateResource(DuplicateResourceException ex) {

		// Maps to HTTP Status Code 409 Conflict
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
		problemDetail.setTitle("Duplicate Resource");

		return problemDetail;
	}

	@ExceptionHandler(InvalidInputException.class)
	public ProblemDetail handleInvalidInput(InvalidInputException ex) {

		// Maps to HTTP Status Code 400 Bad Request
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
		problemDetail.setTitle("Invalid Input");

		return problemDetail;
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {

		// Use ProblemDetail.forStatusAndDetail to create an RFC 7807 compliant body
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, // HTTP Status Code 404
				ex.getMessage() // The detail message from the exception
		);

		// Optional: Add more fields for clarity
		problemDetail.setTitle("Resource Not Found");
		// problemDetail.setType(URI.create("https://example.com/errors/not-found"));

		return problemDetail;
	}

	// A fallback handler for all other unexpected exceptions
	@ExceptionHandler(Exception.class)
	public ProblemDetail handleAllExceptions(Exception ex, WebRequest request) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, // HTTP Status
																											// Code 500
				"An unexpected error occurred.");
		problemDetail.setTitle("Internal Server Error");

		// In a real application, you would log the full stack trace here,
		// but avoid returning it to the client in production.

		return problemDetail;
	}

	@ExceptionHandler(ConflictException.class)
	public ProblemDetail handleConflict(ConflictException ex) {

		// Maps the exception to an HTTP 409 Conflict response
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
		problemDetail.setTitle("Conflict");

		return problemDetail;
	}
}
