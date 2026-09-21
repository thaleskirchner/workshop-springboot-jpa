package com.educandoweb.course.resources.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

/**
 * Covers {@link StandardError}'s accessors, since this DTO shapes the JSON
 * body returned to API clients on every handled error.
 */
class StandardErrorTest {

	@Test
	void constructorAndAccessorsShouldExposeGivenValues() {
		Instant timestamp = Instant.parse("2019-06-20T19:53:07Z");

		StandardError error = new StandardError(timestamp, 404, "Resource not found", "Resource not found. Id 1",
				"/users/1");

		assertThat(error.getTimestamp()).isEqualTo(timestamp);
		assertThat(error.getStatus()).isEqualTo(404);
		assertThat(error.getError()).isEqualTo("Resource not found");
		assertThat(error.getMessage()).isEqualTo("Resource not found. Id 1");
		assertThat(error.getPath()).isEqualTo("/users/1");
	}

	@Test
	void settersShouldUpdateFields() {
		StandardError error = new StandardError();
		Instant timestamp = Instant.parse("2019-06-20T19:53:07Z");

		error.setTimestamp(timestamp);
		error.setStatus(400);
		error.setError("Database error");
		error.setMessage("integrity violation");
		error.setPath("/users/2");

		assertThat(error.getTimestamp()).isEqualTo(timestamp);
		assertThat(error.getStatus()).isEqualTo(400);
		assertThat(error.getError()).isEqualTo("Database error");
		assertThat(error.getMessage()).isEqualTo("integrity violation");
		assertThat(error.getPath()).isEqualTo("/users/2");
	}
}
