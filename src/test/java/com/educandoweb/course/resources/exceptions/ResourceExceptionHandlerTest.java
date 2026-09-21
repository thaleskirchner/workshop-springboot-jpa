package com.educandoweb.course.resources.exceptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.educandoweb.course.services.exceptions.DataBaseException;
import com.educandoweb.course.services.exceptions.ResourceNotFoundException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Unit tests for {@link ResourceExceptionHandler}, verifying that each
 * handled exception is translated into the correct HTTP status and that the
 * response body ({@link StandardError}) carries the exception message and
 * the offending request path.
 */
@ExtendWith(MockitoExtension.class)
class ResourceExceptionHandlerTest {

	@Mock
	private HttpServletRequest request;

	private final ResourceExceptionHandler handler = new ResourceExceptionHandler();

	@Test
	void resourceNotFoundShouldReturn404WithStandardErrorBody() {
		when(request.getRequestURI()).thenReturn("/users/1000");
		ResourceNotFoundException exception = new ResourceNotFoundException(1000L);

		ResponseEntity<StandardError> response = handler.resourceNotFound(exception, request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
		assertThat(response.getBody().getError()).isEqualTo("Resource not found");
		assertThat(response.getBody().getMessage()).isEqualTo("Resource not found. Id 1000");
		assertThat(response.getBody().getPath()).isEqualTo("/users/1000");
		assertThat(response.getBody().getTimestamp()).isNotNull();
	}

	@Test
	void databaseShouldReturn400WithStandardErrorBody() {
		when(request.getRequestURI()).thenReturn("/users/2");
		DataBaseException exception = new DataBaseException("integrity violation");

		ResponseEntity<StandardError> response = handler.database(exception, request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
		assertThat(response.getBody().getError()).isEqualTo("Database error");
		assertThat(response.getBody().getMessage()).isEqualTo("integrity violation");
		assertThat(response.getBody().getPath()).isEqualTo("/users/2");
	}
}
