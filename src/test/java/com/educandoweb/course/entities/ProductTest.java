package com.educandoweb.course.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.educandoweb.course.entities.enums.OrderStatus;

/**
 * Covers {@link Product}'s accessors, its {@code categories} association,
 * the derived {@code getOrders()} that walks the product's order items, and
 * the id-based {@code equals}/{@code hashCode} contract.
 */
class ProductTest {

	@Test
	void constructorAndAccessorsShouldExposeGivenValues() {
		Product product = new Product(1L, "The Lord of the Rings", "desc", 90.5, "img.png");

		assertThat(product.getId()).isEqualTo(1L);
		assertThat(product.getName()).isEqualTo("The Lord of the Rings");
		assertThat(product.getDescription()).isEqualTo("desc");
		assertThat(product.getPrice()).isEqualTo(90.5);
		assertThat(product.getImgUrl()).isEqualTo("img.png");
		assertThat(product.getCategories()).isEmpty();
	}

	@Test
	void settersShouldUpdateFields() {
		Product product = new Product();

		product.setId(2L);
		product.setName("Smart TV");
		product.setDescription("desc2");
		product.setPrice(2190.0);
		product.setImgUrl("tv.png");

		assertThat(product.getId()).isEqualTo(2L);
		assertThat(product.getName()).isEqualTo("Smart TV");
		assertThat(product.getDescription()).isEqualTo("desc2");
		assertThat(product.getPrice()).isEqualTo(2190.0);
		assertThat(product.getImgUrl()).isEqualTo("tv.png");
	}

	@Test
	void categoriesShouldBeMutableSet() {
		Product product = new Product(1L, "p", "d", 1.0, "");
		Category category = new Category(1L, "Books");

		product.getCategories().add(category);

		assertThat(product.getCategories()).containsExactly(category);
	}

	@Test
	void getOrdersShouldReturnDistinctOrdersFromItems() throws Exception {
		// "items" is the inverse side of a @OneToMany (mappedBy), normally
		// populated by Hibernate when loading a Product; reflection mirrors
		// that here so getOrders() can be exercised without a database.
		User client = new User(1L, "Maria Brown", "maria@gmail.com", "988888888", "123456");
		Order order1 = new Order(1L, Instant.parse("2019-06-20T19:53:07Z"), OrderStatus.PAID, client);
		Order order2 = new Order(2L, Instant.parse("2019-07-21T03:42:10Z"), OrderStatus.WAITING_PAYMENT, client);
		Product product = new Product(1L, "p", "d", 1.0, "");

		OrderItem item1 = new OrderItem(order1, product, 1, 1.0);
		OrderItem item2 = new OrderItem(order2, product, 1, 1.0);

		java.lang.reflect.Field itemsField = Product.class.getDeclaredField("items");
		itemsField.setAccessible(true);
		@SuppressWarnings("unchecked")
		java.util.Set<OrderItem> items = (java.util.Set<OrderItem>) itemsField.get(product);
		items.add(item1);
		items.add(item2);

		assertThat(product.getOrders()).containsExactlyInAnyOrder(order1, order2);
	}

	@Test
	void getOrdersShouldReturnEmptySetWhenThereAreNoItems() {
		Product product = new Product(1L, "p", "d", 1.0, "");

		assertThat(product.getOrders()).isEmpty();
	}

	@Test
	void equalsAndHashCodeShouldBeBasedOnId() {
		Product product1 = new Product(1L, "p", "d", 1.0, "");
		Product product2 = new Product(1L, "other", "other", 2.0, "other");
		Product product3 = new Product(2L, "p", "d", 1.0, "");

		assertThat(product1).isEqualTo(product2);
		assertThat(product1).hasSameHashCodeAs(product2);
		assertThat(product1).isNotEqualTo(product3);
		assertThat(product1).isNotEqualTo(null);
		assertThat(product1).isNotEqualTo("not a product");
		assertThat(product1).isEqualTo(product1);
	}
}
