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
 * Full-stack integration tests for the read-only {@code /categories}
 * endpoints, exercising Resource -> Service -> Repository -> Database
 * against the data seeded by {@code TestConfig}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CategoryResourceIT {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void findAllShouldReturnSeededCategories() throws Exception {
		mockMvc.perform(get("/categories"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))))
				.andExpect(jsonPath("$[1].name").value("Books"));
	}

	@Test
	void findByIdShouldReturnCategoryWhenIdExists() throws Exception {
		mockMvc.perform(get("/categories/{id}", 3L))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(3))
				.andExpect(jsonPath("$.name").value("Computers"));
	}

	@Test
	void findByIdShouldFailWhenIdDoesNotExist() {
		// Same existing gap as ProductService: no handled domain exception
		// is thrown for an unknown id, so the exception propagates out of
		// perform() itself instead of yielding a 404 response.
		assertThatThrownBy(() -> mockMvc.perform(get("/categories/{id}", 9999L)))
				.hasCauseInstanceOf(NoSuchElementException.class);
	}
}
