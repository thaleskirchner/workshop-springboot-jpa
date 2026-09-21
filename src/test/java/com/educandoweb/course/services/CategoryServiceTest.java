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

import com.educandoweb.course.entities.Category;
import com.educandoweb.course.repositories.CategoryRepository;

/**
 * Unit tests for {@link CategoryService}, mirroring the coverage strategy
 * used for {@link ProductService}: happy path plus the "not found"
 * behaviour inherited from the direct {@code Optional.get()} call.
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

	@Mock
	private CategoryRepository repository;

	@InjectMocks
	private CategoryService service;

	private final Long existingId = 1L;
	private final Long nonExistingId = 1000L;

	@Test
	void findAllShouldReturnAllCategories() {
		Category category = new Category(existingId, "Electronics");
		when(repository.findAll()).thenReturn(List.of(category));

		List<Category> result = service.findAll();

		assertThat(result).containsExactly(category);
		verify(repository, times(1)).findAll();
	}

	@Test
	void findByIdShouldReturnCategoryWhenIdExists() {
		Category category = new Category(existingId, "Electronics");
		when(repository.findById(existingId)).thenReturn(Optional.of(category));

		Category result = service.findById(existingId);

		assertThat(result).isEqualTo(category);
	}

	@Test
	void findByIdShouldThrowNoSuchElementExceptionWhenIdDoesNotExist() {
		when(repository.findById(nonExistingId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.findById(nonExistingId))
				.isInstanceOf(NoSuchElementException.class);
	}
}
