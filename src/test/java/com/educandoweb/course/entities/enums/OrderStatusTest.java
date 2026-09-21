package com.educandoweb.course.entities.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * {@link OrderStatus} is persisted as an integer code, not a name, so these
 * tests protect the code <-> enum round trip used by {@code Order} when
 * reading and writing the {@code orderStatus} column.
 */
class OrderStatusTest {

	@Test
	void getCodeShouldReturnCodeForEachStatus() {
		assertThat(OrderStatus.WAITING_PAYMENT.getCode()).isEqualTo(1);
		assertThat(OrderStatus.PAID.getCode()).isEqualTo(2);
		assertThat(OrderStatus.SHIPPED.getCode()).isEqualTo(3);
		assertThat(OrderStatus.DELIVERED.getCode()).isEqualTo(4);
		assertThat(OrderStatus.CANCELED.getCode()).isEqualTo(5);
	}

	@Test
	void valueOfShouldReturnMatchingStatusForKnownCode() {
		assertThat(OrderStatus.valueOf(1)).isEqualTo(OrderStatus.WAITING_PAYMENT);
		assertThat(OrderStatus.valueOf(2)).isEqualTo(OrderStatus.PAID);
		assertThat(OrderStatus.valueOf(3)).isEqualTo(OrderStatus.SHIPPED);
		assertThat(OrderStatus.valueOf(4)).isEqualTo(OrderStatus.DELIVERED);
		assertThat(OrderStatus.valueOf(5)).isEqualTo(OrderStatus.CANCELED);
	}

	@Test
	void valueOfShouldThrowIllegalArgumentExceptionForUnknownCode() {
		assertThatThrownBy(() -> OrderStatus.valueOf(99))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Invalid OrderStatus code");
	}
}
