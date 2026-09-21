package com.educandoweb.course.entities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Covers {@link Category}'s accessors, its {@code products} association,
 * and the id-based {@code equals}/{@code hashCode} contract.
 */
class CategoryTest {

	@Test
	void constructorAndAccessorsShouldExposeGivenValues() {
		Category category = new Category(1L, "Electronics");

		assertThat(category.getId()).isEqualTo(1L);
		assertThat(category.getName()).isEqualTo("Electronics");
		assertThat(category.getProducts()).isEmpty();
	}

	@Test
	void settersShouldUpdateFields() {
		Category category = new Category();

		category.setId(2L);
		category.setName("Books");

		assertThat(category.getId()).isEqualTo(2L);
		assertThat(category.getName()).isEqualTo("Books");
	}

	@Test
	void productsShouldBeMutableSet() {
		Category category = new Category(1L, "Books");
		Product product = new Product(1L, "p", "d", 1.0, "");

		category.getProducts().add(product);

		assertThat(category.getProducts()).containsExactly(product);
	}

	@Test
	void equalsAndHashCodeShouldBeBasedOnId() {
		Category category1 = new Category(1L, "Books");
		Category category2 = new Category(1L, "Other name");
		Category category3 = new Category(2L, "Books");

		assertThat(category1).isEqualTo(category2);
		assertThat(category1).hasSameHashCodeAs(category2);
		assertThat(category1).isNotEqualTo(category3);
		assertThat(category1).isNotEqualTo(null);
		assertThat(category1).isNotEqualTo("not a category");
		assertThat(category1).isEqualTo(category1);
	}
}
