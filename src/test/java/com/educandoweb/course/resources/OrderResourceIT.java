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
 * Full-stack integration tests for the read-only {@code /orders} endpoints.
 * These also indirectly verify the {@code Order}/{@code OrderItem}/
 * {@code Payment} JPA mappings, since the JSON responses depend on the
 * composite-key {@code items} association, the {@code getTotal()} business
 * rule and the one-to-one {@code payment} association all being wired
 * correctly against the real H2 database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderResourceIT {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void findAllShouldReturnSeededOrders() throws Exception {
		mockMvc.perform(get("/orders"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));
	}

	@Test
	void findByIdShouldReturnOrderWithItemsTotalAndPaymentWhenIdExists() throws Exception {
		mockMvc.perform(get("/orders/{id}", 1L))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.orderStatus").value("PAID"))
				.andExpect(jsonPath("$.client.name").value("Maria Brown"))
				.andExpect(jsonPath("$.items", hasSize(2)))
				.andExpect(jsonPath("$.total").value(2 * 90.5 + 1250.0))
				.andExpect(jsonPath("$.payment").exists())
				.andExpect(jsonPath("$.payment.moment").value("2019-06-20T21:53:07Z"));
	}

	@Test
	void findByIdShouldReturnOrderWithoutPaymentWhenNotPaid() throws Exception {
		mockMvc.perform(get("/orders/{id}", 2L))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.orderStatus").value("WAITING_PAYMENT"))
				.andExpect(jsonPath("$.payment").doesNotExist());
	}

	@Test
	void findByIdShouldFailWhenIdDoesNotExist() {
		// Same existing gap as ProductService/CategoryService: no handled
		// domain exception is thrown for an unknown id, so the exception
		// propagates out of perform() itself instead of yielding a 404.
		assertThatThrownBy(() -> mockMvc.perform(get("/orders/{id}", 9999L)))
				.hasCauseInstanceOf(NoSuchElementException.class);
	}
}
