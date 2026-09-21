package com.educandoweb.course.entities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Covers {@link User}'s getters/setters and the id-based
 * {@code equals}/{@code hashCode} contract relied upon by JPA and by
 * collections such as {@code Order.client}.
 */
class UserTest {

	@Test
	void constructorAndAccessorsShouldExposeGivenValues() {
		User user = new User(1L, "Maria Brown", "maria@gmail.com", "988888888", "123456");

		assertThat(user.getId()).isEqualTo(1L);
		assertThat(user.getName()).isEqualTo("Maria Brown");
		assertThat(user.getEmail()).isEqualTo("maria@gmail.com");
		assertThat(user.getPhone()).isEqualTo("988888888");
		assertThat(user.getPassword()).isEqualTo("123456");
		assertThat(user.getOrders()).isEmpty();
	}

	@Test
	void settersShouldUpdateFields() {
		User user = new User();

		user.setId(2L);
		user.setName("Alex Green");
		user.setEmail("alex@gmail.com");
		user.setPhone("977777777");
		user.setPassword("abcdef");

		assertThat(user.getId()).isEqualTo(2L);
		assertThat(user.getName()).isEqualTo("Alex Green");
		assertThat(user.getEmail()).isEqualTo("alex@gmail.com");
		assertThat(user.getPhone()).isEqualTo("977777777");
		assertThat(user.getPassword()).isEqualTo("abcdef");
	}

	@Test
	void equalsAndHashCodeShouldBeBasedOnId() {
		User user1 = new User(1L, "Maria Brown", "maria@gmail.com", "988888888", "123456");
		User user2 = new User(1L, "Different Name", "different@gmail.com", "000000000", "xyz");
		User user3 = new User(2L, "Maria Brown", "maria@gmail.com", "988888888", "123456");

		assertThat(user1).isEqualTo(user2);
		assertThat(user1).hasSameHashCodeAs(user2);
		assertThat(user1).isNotEqualTo(user3);
		assertThat(user1).isNotEqualTo(null);
		assertThat(user1).isNotEqualTo("not a user");
		assertThat(user1).isEqualTo(user1);
	}
}
