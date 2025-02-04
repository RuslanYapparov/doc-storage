package ru.yappy.docstorage.model.dto;

public record UserDto(String username,
                      String firstName,
                      String lastName,
                      boolean isEnabled) {}