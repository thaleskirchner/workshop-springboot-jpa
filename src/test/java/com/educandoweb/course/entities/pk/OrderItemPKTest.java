package com.educandoweb.course.entities.pk;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.educandoweb.course.entities.Order;
import com.educandoweb.course.entities.Product;

/**
 * {@link OrderItemPK} is the {@code @EmbeddedId} of {@code OrderItem}, so
 * its {@code equals}/{@code hashCode} contract (based on order + product)
 * directly determines JPA identity and collection semantics for order
 * items; these tests pin down that contract.
 */
class OrderItemPKTest {

	@Test
	void gettersAndSettersShouldWork() {
		OrderItemPK pk = new OrderItemPK();
		Order order = new Order();
		Product product = new Product();

		pk.setOrder(order);
		pk.setProduct(product);

		assertThat(pk.getOrder()).isSameAs(order);
		assertThat(pk.getProduct()).isSameAs(product);
	}

	@Test
	void equalsAndHashCodeShouldBeBasedOnOrderAndProduct() {
		Order order = new Order(1L, null, null, null);
		Product product = new Product(2L, "p", "d", 1.0, "");

		OrderItemPK pk1 = new OrderItemPK();
		pk1.setOrder(order);
		pk1.setProduct(product);

		OrderItemPK pk2 = new OrderItemPK();
		pk2.setOrder(order);
		pk2.setProduct(product);

		OrderItemPK pk3 = new OrderItemPK();
		pk3.setOrder(new Order(3L, null, null, null));
		pk3.setProduct(product);

		assertThat(pk1).isEqualTo(pk2);
		assertThat(pk1).hasSameHashCodeAs(pk2);
		assertThat(pk1).isNotEqualTo(pk3);
		assertThat(pk1).isNotEqualTo(null);
		assertThat(pk1).isNotEqualTo("not a pk");
		assertThat(pk1).isEqualTo(pk1);
	}
}
