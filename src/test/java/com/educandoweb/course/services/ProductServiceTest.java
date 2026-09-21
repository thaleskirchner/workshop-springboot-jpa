package com.educandoweb.course.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.educandoweb.course.entities.Product;
import com.educandoweb.course.repositories.ProductRepository;

/**
 * Unit tests for {@link ProductService}. Covers the happy path (product
 * found) and documents the current behaviour when a product id does not
 * exist: since the service calls {@code Optional.get()} directly, a
 * {@link NoSuchElementException} propagates instead of a domain exception.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

	@Mock
	private ProductRepository repository;

	@InjectMocks
	private ProductService service;

	private final Long existingId = 1L;
	private final Long nonExistingId = 1000L;

	@Test
	void findAllShouldReturnAllProducts() {
		Product product = new Product(existingId, "The Lord of the Rings", "desc", 90.5, "");
		when(repository.findAll()).thenReturn(List.of(product));

		List<Product> result = service.findAll();

		assertThat(result).containsExactly(product);
		verify(repository, times(1)).findAll();
	}

	@Test
	void findByIdShouldReturnProductWhenIdExists() {
		Product product = new Product(existingId, "The Lord of the Rings", "desc", 90.5, "");
		when(repository.findById(existingId)).thenReturn(Optional.of(product));

		Product result = service.findById(existingId);

		assertThat(result).isEqualTo(product);
	}

	@Test
	void findByIdShouldThrowNoSuchElementExceptionWhenIdDoesNotExist() {
		when(repository.findById(nonExistingId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.findById(nonExistingId))
				.isInstanceOf(NoSuchElementException.class);
	}
}
