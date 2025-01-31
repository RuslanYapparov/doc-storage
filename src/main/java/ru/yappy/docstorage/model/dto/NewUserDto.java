package ru.yappy.docstorage.model.dto;

import jakarta.validation.constraints.*;

public record NewUserDto(@NotBlank String username,
                         @NotBlank String password,
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