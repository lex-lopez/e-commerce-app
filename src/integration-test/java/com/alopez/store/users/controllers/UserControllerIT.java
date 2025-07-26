package com.alopez.store.users.controllers;

import com.alopez.store.users.dtos.RegisterUserRequest;
import com.alopez.store.users.dtos.UserDto;
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
}