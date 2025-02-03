package ru.yappy.docstorage.model.dto;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

public record NewUserDto(@NotBlank @Length(min = 3, max = 20) String username,
                         @NotBlank @Length(min = 5, max = 15) String password,
                         @NotNull @Email String email,
                         String firstName,
                         String lastName) {

    @Override
    public String toString() {
        return "UserDto{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }

}