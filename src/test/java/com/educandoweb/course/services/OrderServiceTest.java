package com.educandoweb.course.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.educandoweb.course.entities.Order;
import com.educandoweb.course.entities.User;
import com.educandoweb.course.entities.enums.OrderStatus;
import com.educandoweb.course.repositories.OrderRepository;

/**
 * Unit tests for {@link OrderService}, mirroring the coverage strategy used
 * for {@link ProductService} and {@link CategoryService}.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

	@Mock
	private OrderRepository repository;

	@InjectMocks
	private OrderService service;

	private final Long existingId = 1L;
	private final Long nonExistingId = 1000L;

	@Test
	void findAllShouldReturnAllOrders() {
		Order order = newOrder();
		when(repository.findAll()).thenReturn(List.of(order));

		List<Order> result = service.findAll();

		assertThat(result).containsExactly(order);
		verify(repository, times(1)).findAll();
	}

	@Test
	void findByIdShouldReturnOrderWhenIdExists() {
		Order order = newOrder();
		when(repository.findById(existingId)).thenReturn(Optional.of(order));

		Order result = service.findById(existingId);

		assertThat(result).isEqualTo(order);
	}

	@Test
	void findByIdShouldThrowNoSuchElementExceptionWhenIdDoesNotExist() {
		when(repository.findById(nonExistingId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.findById(nonExistingId))
				.isInstanceOf(NoSuchElementException.class);
	}

	private Order newOrder() {
		User client = new User(1L, "Maria Brown", "maria@gmail.com", "988888888", "123456");
		return new Order(existingId, Instant.parse("2019-06-20T19:53:07Z"), OrderStatus.PAID, client);
	}
}
