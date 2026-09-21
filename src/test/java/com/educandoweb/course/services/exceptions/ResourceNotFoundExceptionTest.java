package com.educandoweb.course.services.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Verifies that {@link ResourceNotFoundException} builds its message from
 * the given id, since that message is what ends up in the API's error
 * response body.
 */
class ResourceNotFoundExceptionTest {

	@Test
	void messageShouldContainGivenId() {
		ResourceNotFoundException exception = new ResourceNotFoundException(10L);

		assertThat(exception.getMessage()).isEqualTo("Resource not found. Id 10");
	}
}
