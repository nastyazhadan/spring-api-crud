package ru.aston.user.service.core;

import ru.aston.user.entity.User;
import ru.aston.user.repository.UserRepository;
import ru.aston.user.util.UserNotFoundException;
import ru.aston.user.util.UserNotCreatedException;
import ru.aston.user.util.UserNotUpdatedException;

import java.util.Optional;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService self;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateUserSuccessfully() {
        User user = new User("Lena", "lena@mail.ru", 25);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User created = userService.createUser(user);

        assertEquals("Lena", created.getName());
        assertEquals("lena@mail.ru", created.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void shouldThrowUserNotCreatedExceptionWhenEmailExists() {
        User user = new User("Lena", "lena@mail.ru", 25);
        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThrows(UserNotCreatedException.class, () -> userService.createUser(user));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void shouldThrowDataAccessExceptionWhenDatabaseFailsOnCreate() {
        User user = new User("Lena", "lena@mail.ru", 25);
        when(userRepository.save(any(User.class)))
                .thenThrow(new DataAccessResourceFailureException("DB down"));

        assertThrows(DataAccessResourceFailureException.class, () -> userService.createUser(user));
    }

    @Test
    void shouldReturnUserById() {
        User user = new User("Alex", "alex@mail.ru", 30);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        User found = userService.getUserById(1);

        assertEquals("Alex", found.getName());
        assertEquals("alex@mail.ru", found.getEmail());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(999));
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        User existing = new User("Old", "old@mail.ru", 20);
        User updated = new User("New", "new@mail.ru", 21);

        when(self.getUserById(1)).thenReturn(existing);
        when(userRepository.save(any(User.class))).thenReturn(updated);

        User result = userService.updateUser(1, updated);

        assertEquals("New", result.getName());
        assertEquals("new@mail.ru", result.getEmail());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingUser() {
        when(self.getUserById(999)).thenThrow(new UserNotFoundException("not found"));

        assertThrows(UserNotFoundException.class,
                () -> userService.updateUser(999, new User("Nastya", "nastya@mail.ru", 10)));
    }

    @Test
    void shouldThrowUserNotUpdatedExceptionWhenSaveFails() {
        User existing = new User("Old", "old@mail.ru", 20);
        User updated = new User("New", "new@mail.ru", 21);

        when(self.getUserById(1)).thenReturn(existing);
        when(userRepository.save(any(User.class)))
                .thenThrow(new RuntimeException("DB write error"));

        assertThrows(UserNotUpdatedException.class, () -> userService.updateUser(1, updated));
    }

    @Test
    void shouldDeleteUserSuccessfully() {
        User user = new User("Kate", "kate@mail.ru", 27);

        when(self.getUserById(1)).thenReturn(user);

        userService.deleteUser(1);

        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingUser() {
        when(self.getUserById(999)).thenThrow(new UserNotFoundException("User not found"));

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(999));

        verify(userRepository, times(0)).delete(any());
    }
}
