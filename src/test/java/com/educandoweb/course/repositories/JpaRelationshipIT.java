package com.educandoweb.course.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.educandoweb.course.entities.Category;
import com.educandoweb.course.entities.Order;
import com.educandoweb.course.entities.OrderItem;
import com.educandoweb.course.entities.Payment;
import com.educandoweb.course.entities.Product;
import com.educandoweb.course.entities.User;
import com.educandoweb.course.entities.enums.OrderStatus;
import com.educandoweb.course.entities.pk.OrderItemPK;

import jakarta.persistence.EntityManager;

/**
 * Integration tests that go straight through the repository layer against
 * the real H2 database (no HTTP involved), to prove that the JPA mappings
 * declared on the entities actually behave as intended once persisted and
 * re-read: the {@code Product}<->{@code Category} many-to-many join table,
 * the {@code Order}<->{@code Payment} one-to-one with a shared/cascaded id,
 * and the {@code OrderItem} composite embedded key. Each test runs in its
 * own rolled-back transaction so it never disturbs the data seeded by
 * {@code TestConfig} for the resource-level integration tests.
 */
@SpringBootTest
@Transactional
class JpaRelationshipIT {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private OrderItemRepository orderItemRepository;

	@Autowired
	private EntityManager entityManager;

	@Test
	void manyToManyBetweenProductAndCategoryShouldBePersistedBothWays() {
		Category category = categoryRepository.save(new Category(null, "Board Games"));
		Product product = new Product(null, "Catan", "desc", 150.0, "");
		product.getCategories().add(category);
		product = productRepository.save(product);

		// Force the associations to be re-read from the database instead of
		// the first-level cache, so this really proves the join table was
		// populated correctly on both sides, not just that Java-side
		// mutations are visible within the same persistence context.
		entityManager.flush();
		entityManager.clear();

		Product reloaded = productRepository.findById(product.getId()).orElseThrow();
		assertThat(reloaded.getCategories()).extracting(Category::getName).containsExactly("Board Games");

		Category reloadedCategory = categoryRepository.findById(category.getId()).orElseThrow();
		assertThat(reloadedCategory.getProducts()).extracting(Product::getId).containsExactly(reloaded.getId());
	}

	@Test
	void oneToOneOrderPaymentShouldCascadeAndShareId() {
		User client = userRepository.save(new User(null, "Test User", "test@user.com", "0", "0"));
		Order order = new Order(null, Instant.now(), OrderStatus.WAITING_PAYMENT, client);
		Payment payment = new Payment(null, Instant.now(), order);
		order.setPayment(payment);

		Order savedOrder = orderRepository.save(order);

		assertThat(savedOrder.getPayment()).isNotNull();
		assertThat(savedOrder.getPayment().getId()).isEqualTo(savedOrder.getId());

		entityManager.flush();
		entityManager.clear();

		Order reloaded = orderRepository.findById(savedOrder.getId()).orElseThrow();
		assertThat(reloaded.getPayment()).isNotNull();
		assertThat(reloaded.getPayment().getId()).isEqualTo(reloaded.getId());
	}

	@Test
	void orderItemShouldBeFoundByItsCompositeEmbeddedId() {
		User client = userRepository.save(new User(null, "Test User 2", "test2@user.com", "0", "0"));
		Order order = orderRepository.save(new Order(null, Instant.now(), OrderStatus.WAITING_PAYMENT, client));
		Product product = productRepository.save(new Product(null, "Widget", "desc", 10.0, ""));

		OrderItem item = new OrderItem(order, product, 3, 10.0);
		orderItemRepository.save(item);

		entityManager.flush();
		entityManager.clear();

		OrderItemPK id = new OrderItemPK();
		id.setOrder(order);
		id.setProduct(product);

		OrderItem reloaded = orderItemRepository.findById(id).orElseThrow();
		assertThat(reloaded.getQuantity()).isEqualTo(3);
		assertThat(reloaded.getSubTotal()).isEqualTo(30.0);
	}
}
