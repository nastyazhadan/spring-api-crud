package ru.aston.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


public class UserDTO {
    @Getter @Setter
    private Integer id;

    @Getter @Setter
    @NotEmpty(message = "Name could not be empty")
    @Size(min = 2, max = 30, message = "Name should be between 2 and 30 chars")
    private String name;

    @Getter @Setter
    @Email(message = "Email should be valid")
    @NotEmpty(message = "Email should not be empty")
    private String email;

    @Getter @Setter
    @Min(value = 0, message = "Age should be more than 0")
    @Max(value = 150, message = "Age should be less than 150")
    private Integer age;
}
