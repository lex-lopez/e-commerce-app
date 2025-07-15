package com.alopez.store.dtos;

import com.alopez.store.validation.Lowercase;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @Size(max = 255, message = "Name must be less than 255 characters")
    private String name;

    @Email(message = "Email must be valid")
    @Lowercase(message = "Email must be in lowercase")
    private String email;
}
