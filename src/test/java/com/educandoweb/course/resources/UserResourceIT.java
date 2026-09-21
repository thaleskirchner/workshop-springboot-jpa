package com.educandoweb.course.resources;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Full-stack integration tests for the {@code /users} endpoints: real Spring
 * context, real {@code UserService} and real H2 database seeded by
 * {@code TestConfig}. They exercise the whole Resource -> Service ->
 * Repository -> Database chain, including the exception-handling path that
 * maps repository outcomes to HTTP responses.
 *
 * <p>Most tests run inside a transaction rolled back after the method
 * ({@code @Transactional}), so the seed data stays intact for every other
 * test class sharing the cached application context. The one test that
 * needs a referential-integrity violation to actually reach the database
 * ({@link #deleteShouldReturnBadRequestWhenUserHasDependentOrders()}) opts
 * out of that wrapping transaction (see its Javadoc) and cleans up the
 * context itself.
 */
@SpringBootTest
@AutoConfigureMockMvc
class UserResourceIT {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@Transactional
	void findAllShouldReturnSeededUsers() throws Exception {
		mockMvc.perform(get("/users"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
				.andExpect(jsonPath("$[0].name").value("Maria Brown"))
				.andExpect(jsonPath("$[1].email").value("alex@gmail.com"));
	}

	@Test
	@Transactional
	void findByIdShouldReturnUserWhenIdExists() throws Exception {
		mockMvc.perform(get("/users/{id}", 1L))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.name").value("Maria Brown"))
				.andExpect(jsonPath("$.email").value("maria@gmail.com"));
	}

	@Test
	@Transactional
	void findByIdShouldReturnStandardErrorWithNotFoundWhenIdDoesNotExist() throws Exception {
		mockMvc.perform(get("/users/{id}", 9999L))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.error").value("Resource not found"))
				.andExpect(jsonPath("$.message").value("Resource not found. Id 9999"))
				.andExpect(jsonPath("$.path").value("/users/9999"));
	}

	@Test
	@Transactional
	void insertShouldPersistUserAndReturnCreated() throws Exception {
		String payload = """
				{
				  "name": "New User",
				  "email": "new.user@email.com",
				  "phone": "11999999999",
				  "password": "password123"
				}
				""";

		mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(payload))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", containsString("/users/")))
				.andExpect(jsonPath("$.id").value(notNullValue()))
				.andExpect(jsonPath("$.name").value("New User"))
				.andExpect(jsonPath("$.email").value("new.user@email.com"));
	}

	@Test
	@Transactional
	void updateShouldReturnUpdatedUserWhenIdExists() throws Exception {
		String payload = """
				{
				  "name": "Maria Brown Updated",
				  "email": "maria.updated@gmail.com",
				  "phone": "999999999"
				}
				""";

		mockMvc.perform(put("/users/{id}", 1L).contentType(MediaType.APPLICATION_JSON).content(payload))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.name").value("Maria Brown Updated"))
				.andExpect(jsonPath("$.email").value("maria.updated@gmail.com"));

		mockMvc.perform(get("/users/{id}", 1L))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Maria Brown Updated"));
	}

	@Test
	@Transactional
	void updateShouldReturnStandardErrorWithNotFoundWhenIdDoesNotExist() throws Exception {
		String payload = """
				{
				  "name": "Does not matter",
				  "email": "x@x.com",
				  "phone": "0"
				}
				""";

		mockMvc.perform(put("/users/{id}", 9999L).contentType(MediaType.APPLICATION_JSON).content(payload))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("Resource not found"));
	}

	@Test
	@Transactional
	void deleteShouldReturnNoContentWhenUserHasNoDependentOrders() throws Exception {
		String payload = """
				{
				  "name": "Disposable User",
				  "email": "disposable@email.com",
				  "phone": "0",
				  "password": "x"
				}
				""";

		String location = mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(payload))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getHeader("Location");
		Long newId = Long.valueOf(location.substring(location.lastIndexOf('/') + 1));

		mockMvc.perform(delete("/users/{id}", newId)).andExpect(status().isNoContent());
		mockMvc.perform(get("/users/{id}", newId)).andExpect(status().isNotFound());
	}

	@Test
	@Transactional
	void deleteShouldReturnNoContentWhenIdDoesNotExist() throws Exception {
		// The current Spring Data JPA deleteById() implementation does not
		// perform an existence check before deleting, so UserService's
		// EmptyResultDataAccessException -> ResourceNotFoundException branch
		// is not reachable through the real repository (it is still
		// exercised, on purpose, by the mocked UserServiceTest). This test
		// documents the actual, current HTTP behaviour: a delete of an
		// unknown id succeeds silently.
		mockMvc.perform(delete("/users/{id}", 9999L)).andExpect(status().isNoContent());
	}

	/**
	 * Deliberately NOT wrapped in the class's usual per-method
	 * {@code @Transactional}: the referential-integrity violation this test
	 * relies on is only raised by H2 when the {@code DELETE} statement is
	 * actually flushed to the database, and a test-managed transaction
	 * defers that flush until its (here: rolled-back) commit, which would
	 * hide the failure. Propagation.NOT_SUPPORTED runs the test outside any
	 * test-managed transaction so the request's own transaction commits (and
	 * fails) for real; {@code @DirtiesContext} then resets the shared
	 * context as a safety net for any test class that reuses it afterwards.
	 */
	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	@DirtiesContext
	void deleteShouldReturnBadRequestWhenUserHasDependentOrders() throws Exception {
		mockMvc.perform(delete("/users/{id}", 1L))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("Database error"));
	}
}
