package com.nikita.gamergram.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Immutable input DTO; Bean Validation works on record components.
public record UserRegistrationRequest(

        @NotBlank(message = "username is required")
        @Size(min = 3, max = 50, message = "username must be 3 to 50 characters")
        String username,

        @NotBlank(message = "email is required")
        @Email(message = "invalid email format")
        String email,

        @NotBlank(message = "password is required")
        @Size(min = 8, max = 100, message = "password must be 8 to 100 characters")
        String password
) {
}
