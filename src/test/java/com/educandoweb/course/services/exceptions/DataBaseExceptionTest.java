package com.educandoweb.course.services.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Verifies that {@link DataBaseException} simply preserves the message it
 * was created with.
 */
class DataBaseExceptionTest {

	@Test
	void messageShouldMatchGivenMessage() {
		DataBaseException exception = new DataBaseException("integrity violation");

		assertThat(exception.getMessage()).isEqualTo("integrity violation");
	}
}
