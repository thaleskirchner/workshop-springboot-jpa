package com.educandoweb.course.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;

import com.educandoweb.course.entities.User;
import com.educandoweb.course.repositories.UserRepository;
import com.educandoweb.course.services.exceptions.DataBaseException;
import com.educandoweb.course.services.exceptions.ResourceNotFoundException;

import jakarta.persistence.EntityNotFoundException;

/**
 * Unit tests for {@link UserService}. The repository is mocked so each test
 * isolates the service's own logic: how it maps repository outcomes
 * (empty results, integrity violations, missing references) onto the
 * application's domain exceptions.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository repository;

	@InjectMocks
	private UserService service;

	private User user;
	private final Long existingId = 1L;
	private final Long nonExistingId = 1000L;
	private final Long dependentId = 2L;

	@BeforeEach
	void setUp() {
		user = new User(existingId, "Maria Brown", "maria@gmail.com", "988888888", "123456");
	}

	@Test
	void findAllShouldReturnAllUsers() {
		when(repository.findAll()).thenReturn(List.of(user));

		List<User> result = service.findAll();

		assertThat(result).containsExactly(user);
		verify(repository, times(1)).findAll();
	}

	@Test
	void findByIdShouldReturnUserWhenIdExists() {
		when(repository.findById(existingId)).thenReturn(Optional.of(user));

		User result = service.findById(existingId);

		assertThat(result).isEqualTo(user);
	}

	@Test
	void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		when(repository.findById(nonExistingId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.findById(nonExistingId))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void insertShouldPersistAndReturnUser() {
		when(repository.save(any(User.class))).thenReturn(user);

		User result = service.insert(new User(null, "Maria Brown", "maria@gmail.com", "988888888", "123456"));

		assertThat(result).isEqualTo(user);
		verify(repository, times(1)).save(any(User.class));
	}

	@Test
	void deleteShouldDoNothingWhenIdExists() {
		doNothing().when(repository).deleteById(existingId);

		service.delete(existingId);

		verify(repository, times(1)).deleteById(existingId);
	}

	@Test
	void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		doThrow(EmptyResultDataAccessException.class).when(repository).deleteById(nonExistingId);

		assertThatThrownBy(() -> service.delete(nonExistingId))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void deleteShouldThrowDataBaseExceptionWhenIdIsDependent() {
		doThrow(new DataIntegrityViolationException("integrity violation")).when(repository).deleteById(dependentId);

		assertThatThrownBy(() -> service.delete(dependentId))
				.isInstanceOf(DataBaseException.class);
	}

	@Test
	void updateShouldReturnUpdatedUserWhenIdExists() {
		when(repository.getReferenceById(existingId)).thenReturn(user);
		when(repository.save(any(User.class))).thenReturn(user);

		User update = new User(null, "New Name", "new@gmail.com", "999999999", null);
		User result = service.update(existingId, update);

		assertThat(result.getName()).isEqualTo("New Name");
		assertThat(result.getEmail()).isEqualTo("new@gmail.com");
		assertThat(result.getPhone()).isEqualTo("999999999");
		verify(repository, times(1)).save(user);
	}

	@Test
	void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		when(repository.getReferenceById(eq(nonExistingId))).thenThrow(EntityNotFoundException.class);

		assertThatThrownBy(() -> service.update(nonExistingId, user))
				.isInstanceOf(ResourceNotFoundException.class);
		verify(repository, never()).save(any(User.class));
	}

	@Test
	void findByIdShouldQueryRepositoryUsingGivenId() {
		when(repository.findById(anyLong())).thenReturn(Optional.of(user));

		service.findById(existingId);

		verify(repository).findById(existingId);
	}
}
