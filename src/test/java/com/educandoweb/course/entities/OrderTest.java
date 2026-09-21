package com.educandoweb.course.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.educandoweb.course.entities.enums.OrderStatus;

/**
 * Covers {@link Order}'s accessors, the integer-backed
 * {@code orderStatus} <-> {@link OrderStatus} conversion, the
 * {@code getTotal()} business rule, and the id/client/moment-based
 * {@code equals}/{@code hashCode} contract.
 */
class OrderTest {

	private final User client = new User(1L, "Maria Brown", "maria@gmail.com", "988888888", "123456");
	private final Instant moment = Instant.parse("2019-06-20T19:53:07Z");

	@Test
	void constructorAndAccessorsShouldExposeGivenValues() {
		Order order = new Order(1L, moment, OrderStatus.PAID, client);

		assertThat(order.getId()).isEqualTo(1L);
		assertThat(order.getMoment()).isEqualTo(moment);
		assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAID);
		assertThat(order.getClient()).isEqualTo(client);
		assertThat(order.getItems()).isEmpty();
		assertThat(order.getPayment()).isNull();
	}

	@Test
	void setOrderStatusShouldIgnoreNullValue() {
		Order order = new Order(1L, moment, OrderStatus.PAID, client);

		order.setOrderStatus(null);

		assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAID);
	}

	@Test
	void setOrderStatusShouldUpdateUnderlyingCode() {
		Order order = new Order(1L, moment, OrderStatus.WAITING_PAYMENT, client);

		order.setOrderStatus(OrderStatus.DELIVERED);

		assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.DELIVERED);
	}

	@Test
	void settersShouldUpdateFields() {
		Order order = new Order();
		Payment payment = new Payment(1L, moment, order);

		order.setId(2L);
		order.setMoment(moment);
		order.setClient(client);
		order.setPayment(payment);

		assertThat(order.getId()).isEqualTo(2L);
		assertThat(order.getMoment()).isEqualTo(moment);
		assertThat(order.getClient()).isEqualTo(client);
		assertThat(order.getPayment()).isEqualTo(payment);
	}

	@Test
	void getTotalShouldSumSubTotalsOfAllItems() {
		Order order = new Order(1L, moment, OrderStatus.PAID, client);
		Product product1 = new Product(1L, "The Lord of the Rings", "d", 90.5, "");
		Product product2 = new Product(2L, "Macbook Pro", "d", 1250.0, "");
		order.getItems().add(new OrderItem(order, product1, 2, 90.5));
		order.getItems().add(new OrderItem(order, product2, 1, 1250.0));

		assertThat(order.getTotal()).isEqualTo(2 * 90.5 + 1250.0);
	}

	@Test
	void getTotalShouldReturnZeroWhenThereAreNoItems() {
		Order order = new Order(1L, moment, OrderStatus.PAID, client);

		assertThat(order.getTotal()).isEqualTo(0.0);
	}

	@Test
	void equalsAndHashCodeShouldBeBasedOnIdClientAndMoment() {
		Order order1 = new Order(1L, moment, OrderStatus.PAID, client);
		Order order2 = new Order(1L, moment, OrderStatus.WAITING_PAYMENT, client);
		Order order3 = new Order(2L, moment, OrderStatus.PAID, client);

		assertThat(order1).isEqualTo(order2);
		assertThat(order1).hasSameHashCodeAs(order2);
		assertThat(order1).isNotEqualTo(order3);
		assertThat(order1).isNotEqualTo(null);
		assertThat(order1).isNotEqualTo("not an order");
		assertThat(order1).isEqualTo(order1);
	}
}
