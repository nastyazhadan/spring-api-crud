package ru.aston.user.service.core;

import ru.aston.common.dto.UserEvent;
import ru.aston.common.dto.UserEventType;
import ru.aston.user.service.messaging.UserEventProducer;
import ru.aston.user.entity.User;
import ru.aston.user.repository.UserRepository;
import ru.aston.user.util.UserNotUpdatedException;
import ru.aston.user.util.UserNotCreatedException;
import ru.aston.user.util.UserNotDeletedException;
import ru.aston.user.util.UserNotFoundException;

import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final UserEventProducer producer;
    private final UserService self;

    @Autowired
    public UserService(UserRepository userRepository, UserEventProducer producer, @Lazy UserService self) {
        this.userRepository = userRepository;
        this.producer = producer;
        this.self = self;
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + id + " not found"));
    }

    @Transactional
    public User createUser(User user) {
        try {
            User savedUser = userRepository.save(user);
            producer.sendEvent(new UserEvent(savedUser.getEmail(), UserEventType.CREATED));
            return savedUser;
        } catch (DataIntegrityViolationException | ConstraintViolationException e) {
            throw new UserNotCreatedException("User with this email " + user.getEmail() + " already exists");
        } catch (DataAccessException dataAccessException) {
            throw dataAccessException;
        } catch (Exception e) {
            throw new UserNotCreatedException(e.getMessage());
        }
    }

    @Transactional
    public User updateUser(int id, User updatedUser) {
        try {
            User existingUser = self.getUserById(id);
            existingUser.setName(updatedUser.getName());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setAge(updatedUser.getAge());
            return userRepository.save(existingUser);
        } catch (DataAccessException | UserNotFoundException exception) {
            throw exception;
        } catch (Exception e) {
            throw new UserNotUpdatedException(e.getMessage());
        }
    }

    @Transactional
    public void deleteUser(int id) {
        try {
            User user = self.getUserById(id);
            userRepository.delete(user);
            producer.sendEvent(new UserEvent(user.getEmail(), UserEventType.DELETED));
        } catch (DataAccessException | UserNotFoundException exception) {
            throw exception;
        } catch (Exception e) {
            throw new UserNotDeletedException(e.getMessage());
        }
    }
}
