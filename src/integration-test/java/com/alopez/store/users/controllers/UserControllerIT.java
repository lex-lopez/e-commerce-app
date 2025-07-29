package com.alopez.store.users.controllers;

import com.alopez.store.users.dtos.ChangePasswordRequest;
import com.alopez.store.users.dtos.RegisterUserRequest;
import com.alopez.store.users.dtos.UpdateUserRequest;
import com.alopez.store.users.dtos.UserDto;
import com.alopez.store.users.exceptions.EmailAlreadyExistsException;
import com.alopez.store.users.exceptions.UserNotAuthorizedException;
import com.alopez.store.users.exceptions.UserNotFoundException;
import com.alopez.store.users.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerIT {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Create standalone MockMvc - no Spring context needed
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void httpGetUser_MapsPathVariableCorrectly() throws Exception {
        // Given
        when(userService.getUserById(123L)).thenReturn(new UserDto(123L, "Test", "test@example.com"));

        // When & Then
        mockMvc.perform(get("/api/users/123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(123))
                .andExpect(jsonPath("$.name").value("Test"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).getUserById(123L);
    }

    @Test
    void httpCreateUser_ReturnsLocationHeader() throws Exception {
        // Given
        RegisterUserRequest request = new RegisterUserRequest();
        request.setName("John");
        request.setEmail("john@example.com");
        request.setPassword("password123");

        when(userService.createUser(any())).thenReturn(new UserDto(42L, "John", "john@example.com"));

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", containsString("/users/42")));
    }

    @Test
    void httpExceptionHandling_MapsToCorrectHttpStatus() throws Exception {
        // Given
        when(userService.getUserById(999L)).thenThrow(new UserNotFoundException());

        // When & Then
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("User not found!"));
    }

    @Test
    void httpGetUsers_WithSortParameter() throws Exception {
        // Given
        when(userService.getAllUsers("name")).thenReturn(
                java.util.List.of(new UserDto(1L, "Alice", "alice@example.com"),
                                 new UserDto(2L, "Bob", "bob@example.com")));

        // When & Then
        mockMvc.perform(get("/api/users").param("sort", "name"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[1].name").value("Bob"));

        verify(userService).getAllUsers("name");
    }

    @Test
    void httpGetUsers_WithoutSortParameter_UsesDefault() throws Exception {
        // Given
        when(userService.getAllUsers("")).thenReturn(
                java.util.List.of(new UserDto(1L, "Test User", "test@example.com")));

        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("Test User"));

        verify(userService).getAllUsers("");
    }

    @Test
    void httpDeleteUser_ReturnsNoContent() throws Exception {
        // Given
        doNothing().when(userService).deleteUser(1L);

        // When & Then
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    void httpUpdateUser_ReturnsUpdatedUser() throws Exception {
        // Given
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setName("Updated John");
        updateRequest.setEmail("john.updated@example.com");

        // Create the expected response that reflects the updated values
        UserDto updatedUser = new UserDto(1L, "Updated John", "john.updated@example.com");

        when(userService.updateUser(eq(1L), any(UpdateUserRequest.class))).thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated John"))
                .andExpect(jsonPath("$.email").value("john.updated@example.com"));

        verify(userService).updateUser(eq(1L), any(UpdateUserRequest.class));
    }

    @Test
    void httpUpdateUser_WithNonExistingUser_ReturnsNotFound() throws Exception {
        // Given
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setName("Updated User");
        updateRequest.setEmail("updated@example.com");

        when(userService.updateUser(eq(999L), any(UpdateUserRequest.class))).thenThrow(new UserNotFoundException());

        // When & Then
        mockMvc.perform(put("/api/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("User not found!"));
    }

    @Test
    void httpUpdateUser_WithExistingEmail_ReturnsBadRequest() throws Exception {
        // Given
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setName("John");
        updateRequest.setEmail("existing@example.com");

        when(userService.updateUser(eq(1L), any(UpdateUserRequest.class)))
                .thenThrow(new EmailAlreadyExistsException());

        // When & Then
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Email is already registered!"));
    }

    @Test
    void httpChangePassword_ReturnsNoContent() throws Exception {
        // Given
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setOldPassword("oldPassword123");
        changePasswordRequest.setNewPassword("newPassword456");

        doNothing().when(userService).changePassword(1L, "oldPassword123", "newPassword456");

        // When & Then
        mockMvc.perform(post("/api/users/1/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isNoContent());

        verify(userService).changePassword(1L, "oldPassword123", "newPassword456");
    }

    @Test
    void httpChangePassword_WithWrongOldPassword_ReturnsUnauthorized() throws Exception {
        // Given
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setOldPassword("wrongPassword");
        changePasswordRequest.setNewPassword("newPassword456");

        doThrow(new UserNotAuthorizedException()).when(userService)
                .changePassword(1L, "wrongPassword", "newPassword456");

        // When & Then
        mockMvc.perform(post("/api/users/1/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("User not authorized!"));
    }

    @Test
    void httpChangePassword_WithNonExistingUser_ReturnsNotFound() throws Exception {
        // Given
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setOldPassword("oldPassword123");
        changePasswordRequest.setNewPassword("newPassword456");

        doThrow(new UserNotFoundException()).when(userService)
                .changePassword(999L, "oldPassword123", "newPassword456");

        // When & Then
        mockMvc.perform(post("/api/users/999/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("User not found!"));
    }
}