package com.educandoweb.course.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.educandoweb.course.entities.enums.OrderStatus;

/**
 * Covers {@link OrderItem}'s composite-key-backed accessors, the
 * {@code getSubTotal()} business rule, and the id-based
 * {@code equals}/{@code hashCode} contract.
 */
class OrderItemTest {

	private final User client = new User(1L, "Maria Brown", "maria@gmail.com", "988888888", "123456");
	private final Order order = new Order(1L, Instant.parse("2019-06-20T19:53:07Z"), OrderStatus.PAID, client);
	private final Product product = new Product(1L, "The Lord of the Rings", "d", 90.5, "");

	@Test
	void constructorAndAccessorsShouldExposeGivenValues() {
		OrderItem item = new OrderItem(order, product, 2, 90.5);

		assertThat(item.getOrder()).isEqualTo(order);
		assertThat(item.getProduct()).isEqualTo(product);
		assertThat(item.getQuantity()).isEqualTo(2);
		assertThat(item.getPrice()).isEqualTo(90.5);
	}

	@Test
	void settersShouldUpdateFields() {
		OrderItem item = new OrderItem();
		Product other = new Product(2L, "Macbook Pro", "d", 1250.0, "");
		Order otherOrder = new Order(2L, null, OrderStatus.WAITING_PAYMENT, client);

		item.setOrder(otherOrder);
		item.setProduct(other);
		item.setQuantity(3);
		item.setPrice(1250.0);

		assertThat(item.getOrder()).isEqualTo(otherOrder);
		assertThat(item.getProduct()).isEqualTo(other);
		assertThat(item.getQuantity()).isEqualTo(3);
		assertThat(item.getPrice()).isEqualTo(1250.0);
	}

	@Test
	void getSubTotalShouldMultiplyPriceByQuantity() {
		OrderItem item = new OrderItem(order, product, 2, 90.5);

		assertThat(item.getSubTotal()).isEqualTo(181.0);
	}

	@Test
	void idGetterAndSetterShouldWork() {
		OrderItem item = new OrderItem(order, product, 1, 90.5);
		var newId = item.getId();
		item.setId(newId);

		assertThat(item.getId()).isEqualTo(newId);
	}

	@Test
	void equalsAndHashCodeShouldBeBasedOnId() {
		OrderItem item1 = new OrderItem(order, product, 2, 90.5);
		OrderItem item2 = new OrderItem(order, product, 99, 1.0);
		OrderItem item3 = new OrderItem(order, new Product(2L, "other", "d", 1.0, ""), 2, 90.5);

		assertThat(item1).isEqualTo(item2);
		assertThat(item1).hasSameHashCodeAs(item2);
		assertThat(item1).isNotEqualTo(item3);
		assertThat(item1).isNotEqualTo(null);
		assertThat(item1).isNotEqualTo("not an item");
		assertThat(item1).isEqualTo(item1);
	}
}
