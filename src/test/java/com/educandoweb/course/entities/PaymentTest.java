package com.educandoweb.course.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.educandoweb.course.entities.enums.OrderStatus;

/**
 * Covers {@link Payment}'s accessors and the id-based
 * {@code equals}/{@code hashCode} contract (the payment shares its id with
 * its order via {@code @MapsId}, but {@code equals} only compares the id
 * field itself).
 */
class PaymentTest {

	private final User client = new User(1L, "Maria Brown", "maria@gmail.com", "988888888", "123456");
	private final Order order = new Order(1L, Instant.parse("2019-06-20T19:53:07Z"), OrderStatus.PAID, client);
	private final Instant moment = Instant.parse("2019-06-20T21:53:07Z");

	@Test
	void constructorAndAccessorsShouldExposeGivenValues() {
		Payment payment = new Payment(1L, moment, order);

		assertThat(payment.getId()).isEqualTo(1L);
		assertThat(payment.getMoment()).isEqualTo(moment);
		assertThat(payment.getOrder()).isEqualTo(order);
	}

	@Test
	void settersShouldUpdateFields() {
		Payment payment = new Payment();
		Order otherOrder = new Order(2L, null, OrderStatus.WAITING_PAYMENT, client);

		payment.setId(2L);
		payment.setMoment(moment);
		payment.setOrder(otherOrder);

		assertThat(payment.getId()).isEqualTo(2L);
		assertThat(payment.getMoment()).isEqualTo(moment);
		assertThat(payment.getOrder()).isEqualTo(otherOrder);
	}

	@Test
	void equalsAndHashCodeShouldBeBasedOnId() {
		Payment payment1 = new Payment(1L, moment, order);
		Payment payment2 = new Payment(1L, null, null);
		Payment payment3 = new Payment(2L, moment, order);

		assertThat(payment1).isEqualTo(payment2);
		assertThat(payment1).hasSameHashCodeAs(payment2);
		assertThat(payment1).isNotEqualTo(payment3);
		assertThat(payment1).isNotEqualTo(null);
		assertThat(payment1).isNotEqualTo("not a payment");
		assertThat(payment1).isEqualTo(payment1);
	}
}
