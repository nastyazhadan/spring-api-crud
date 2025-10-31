package ru.aston.user.controller;

import ru.aston.user.dto.UserDTO;
import ru.aston.user.entity.User;
import ru.aston.user.service.core.UserService;
import ru.aston.user.util.UserErrorResponse;
import ru.aston.user.util.UserNotCreatedException;
import ru.aston.user.util.UserNotUpdatedException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.BindingResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import java.util.List;


@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final ModelMapper modelMapper;

    @Autowired
    public UserController(UserService userService, ModelMapper modelMapper) {
        this.userService = userService;
        this.modelMapper = modelMapper;
    }

    @GetMapping()
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers().stream()
                                        .map(this::convertToUserDTO)
                                        .toList();
    }

    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable("id") int id) {
        return convertToUserDTO(userService.getUserById(id));
    }

    @PostMapping()
    public ResponseEntity<UserDTO> createUser(@RequestBody @Valid UserDTO userDTO,
                                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new UserNotCreatedException(UserErrorResponse.getErrorMessage(bindingResult));
        }

        User createdUser = userService.createUser(convertToUser(userDTO));

        return new ResponseEntity<>(convertToUserDTO(createdUser), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser (@PathVariable("id") int id,
                                               @RequestBody @Valid UserDTO userDTO,
                                               BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new UserNotUpdatedException(UserErrorResponse.getErrorMessage(bindingResult));
        }

        User updatedUser = userService.updateUser(id, convertToUser(userDTO));

        return new ResponseEntity<>(convertToUserDTO(updatedUser), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable int id) {
        userService.deleteUser(id);

        return ResponseEntity.ok().body("User deleted successfully");
    }

    private User convertToUser(UserDTO userDTO) {
        return modelMapper.map(userDTO, User.class);
    }

    private UserDTO convertToUserDTO(User user) {
        return modelMapper.map(user, UserDTO.class);
    }
}
