package com.alopez.store.users.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank(message = "Old Password is required!")
    private String oldPassword;

    @NotBlank(message = "New Password is required!")
    private String newPassword;
}
