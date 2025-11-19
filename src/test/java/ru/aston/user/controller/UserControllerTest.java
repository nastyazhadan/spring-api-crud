package ru.aston.user.controller;

import ru.aston.user.dto.UserDTO;
import ru.aston.user.entity.User;
import ru.aston.user.service.core.UserService;
import ru.aston.user.util.UserNotCreatedException;
import ru.aston.user.util.UserNotUpdatedException;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserController userController;

    @Test
    void shouldReturnListOfUserDTO() {
        User user1 = new User();
        user1.setId(1);
        user1.setName("Inna");
        user1.setEmail("inna@mail.ru");
        user1.setAge(28);

        User user2 = new User();
        user2.setId(2);
        user2.setName("Anna");
        user2.setEmail("anna@mail.ru");
        user2.setAge(31);

        when(userService.getAllUsers()).thenReturn(List.of(user1, user2));

        UserDTO dto1 = new UserDTO();
        dto1.setId(1);
        dto1.setName("Inna");
        dto1.setEmail("inna@mail.ru");
        dto1.setAge(28);

        UserDTO dto2 = new UserDTO();
        dto2.setId(2);
        dto2.setName("Anna");
        dto2.setEmail("anna@mail.ru");
        dto2.setAge(31);

        when(modelMapper.map(user1, UserDTO.class)).thenReturn(dto1);
        when(modelMapper.map(user2, UserDTO.class)).thenReturn(dto2);

        List<UserDTO> result = userController.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("Inna", result.get(0).getName());
        assertEquals("Anna", result.get(1).getName());
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void shouldReturnUserDTO() {
        int id = 1;
        User user = new User();
        user.setId(id);
        user.setName("Lena");
        user.setEmail("lena@mail.ru");
        user.setAge(25);

        UserDTO dto = new UserDTO();
        dto.setId(id);
        dto.setName("Lena");
        dto.setEmail("lena@mail.ru");
        dto.setAge(25);

        when(userService.getUserById(id)).thenReturn(user);
        when(modelMapper.map(user, UserDTO.class)).thenReturn(dto);

        UserDTO result = userController.getUserById(id);

        assertNotNull(result);
        assertEquals("Lena", result.getName());
        assertEquals("lena@mail.ru", result.getEmail());
        verify(userService, times(1)).getUserById(id);
    }

    @Test
    void shouldReturnCreatedDTOIfValidRequest() {
        UserDTO requestDto = new UserDTO();
        requestDto.setName("Nastya");
        requestDto.setEmail("nastya@mail.ru");
        requestDto.setAge(30);

        BindingResult bindingResult = new BeanPropertyBindingResult(requestDto, "userDTO");

        User userToSave = new User();
        userToSave.setName("Nastya");
        userToSave.setEmail("nastya@mail.ru");
        userToSave.setAge(30);

        User savedUser = new User();
        savedUser.setId(1);
        savedUser.setName("Nastya");
        savedUser.setEmail("nastya@mail.ru");
        savedUser.setAge(30);

        UserDTO responseDto = new UserDTO();
        responseDto.setId(1);
        responseDto.setName("Nastya");
        responseDto.setEmail("nastya@mail.ru");
        responseDto.setAge(30);

        when(modelMapper.map(requestDto, User.class)).thenReturn(userToSave);
        when(userService.createUser(userToSave)).thenReturn(savedUser);
        when(modelMapper.map(savedUser, UserDTO.class)).thenReturn(responseDto);

        ResponseEntity<UserDTO> response = userController.createUser(requestDto, bindingResult);

        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Nastya", response.getBody().getName());
        verify(userService, times(1)).createUser(userToSave);
    }

    @Test
    void shouldThrowUserNotCreatedException() {
        UserDTO requestDto = new UserDTO();
        requestDto.setName("");
        requestDto.setEmail("wrong-email");
        requestDto.setAge(-1);

        BindingResult bindingResult = new BeanPropertyBindingResult(requestDto, "userDTO");
        bindingResult.rejectValue("name", "NotBlank", "Name should not be empty");

        assertThrows(UserNotCreatedException.class,
                () -> userController.createUser(requestDto, bindingResult));

        verifyNoInteractions(userService);
    }

    @Test
    void shouldReturnUpdatedDTOIfValidRequest() {
        int id = 5;

        UserDTO requestDto = new UserDTO();
        requestDto.setName("Updated");
        requestDto.setEmail("updated@mail.ru");
        requestDto.setAge(35);

        BindingResult bindingResult = new BeanPropertyBindingResult(requestDto, "userDTO");

        User userToUpdate = new User();
        userToUpdate.setName("Updated");
        userToUpdate.setEmail("updated@mail.ru");
        userToUpdate.setAge(35);

        User updatedUser = new User();
        updatedUser.setId(id);
        updatedUser.setName("Updated");
        updatedUser.setEmail("updated@mail.ru");
        updatedUser.setAge(35);

        UserDTO responseDto = new UserDTO();
        responseDto.setId(id);
        responseDto.setName("Updated");
        responseDto.setEmail("updated@mail.ru");
        responseDto.setAge(35);

        when(modelMapper.map(requestDto, User.class)).thenReturn(userToUpdate);
        when(userService.updateUser(id, userToUpdate)).thenReturn(updatedUser);
        when(modelMapper.map(updatedUser, UserDTO.class)).thenReturn(responseDto);

        ResponseEntity<UserDTO> response = userController.updateUser(id, requestDto, bindingResult);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Updated", response.getBody().getName());
        verify(userService, times(1)).updateUser(id, userToUpdate);
    }

    @Test
    void shouldThrowUserNotUpdatedException() {
        int id = 5;
        UserDTO requestDto = new UserDTO();
        requestDto.setName("");
        requestDto.setEmail("wrong");
        requestDto.setAge(-10);

        BindingResult bindingResult = new BeanPropertyBindingResult(requestDto, "userDTO");
        bindingResult.rejectValue("age", "Positive", "Age should be more than 0");

        assertThrows(UserNotUpdatedException.class,
                () -> userController.updateUser(id, requestDto, bindingResult));

        verifyNoInteractions(userService);
    }

    @Test
    void shouldCallServiceAndReturnOk() {
        int id = 10;

        ResponseEntity<String> response = userController.delete(id);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("User deleted successfully", response.getBody());
        verify(userService, times(1)).deleteUser(id);
    }
}
