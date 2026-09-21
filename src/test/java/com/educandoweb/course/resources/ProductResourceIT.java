package com.educandoweb.course.resources;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Full-stack integration tests for the read-only {@code /products}
 * endpoints, exercising Resource -> Service -> Repository -> Database
 * against the data seeded by {@code TestConfig}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductResourceIT {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void findAllShouldReturnSeededProducts() throws Exception {
		mockMvc.perform(get("/products"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(5))))
				.andExpect(jsonPath("$[0].name").value("The Lord of the Rings"));
	}

	@Test
	void findByIdShouldReturnProductWithCategoriesWhenIdExists() throws Exception {
		mockMvc.perform(get("/products/{id}", 2L))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(2))
				.andExpect(jsonPath("$.name").value("Smart TV"))
				.andExpect(jsonPath("$.price").value(2190.0))
				.andExpect(jsonPath("$.categories", hasSize(2)));
	}

	@Test
	void findByIdShouldFailWhenIdDoesNotExist() {
		// ProductService#findById calls Optional.get() directly instead of
		// throwing a handled ResourceNotFoundException. No
		// HandlerExceptionResolver is registered for NoSuchElementException,
		// so under MockMvc it propagates out of perform() itself (wrapped in
		// a ServletException) instead of yielding a 404 response. This test
		// pins down that existing behaviour.
		assertThatThrownBy(() -> mockMvc.perform(get("/products/{id}", 9999L)))
				.hasCauseInstanceOf(NoSuchElementException.class);
	}
}
