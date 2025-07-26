package com.alopez.store.users.services;

import com.alopez.store.users.dtos.RegisterUserRequest;
import com.alopez.store.users.dtos.UpdateUserRequest;
import com.alopez.store.users.dtos.UserDto;
import com.alopez.store.users.entities.Role;
import com.alopez.store.users.entities.User;
import com.alopez.store.users.exceptions.EmailAlreadyExistsException;
import com.alopez.store.users.exceptions.UserNotAuthorizedException;
import com.alopez.store.users.exceptions.UserNotFoundException;
import com.alopez.store.users.mappers.UserMapper;
import com.alopez.store.users.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDto testUserDto;
    private RegisterUserRequest registerRequest;
    private UpdateUserRequest updateRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.USER);

        testUserDto = new UserDto(1L, "John Doe", "john@example.com");

        registerRequest = new RegisterUserRequest();
        registerRequest.setName("Jane Doe");
        registerRequest.setEmail("jane@example.com");
        registerRequest.setPassword("password123");

        updateRequest = new UpdateUserRequest();
        updateRequest.setName("John Updated");
        updateRequest.setEmail("john.updated@example.com");
    }

    @Test
    void getAllUsers_WithValidSort_ReturnsUserList() {
        // Given
        List<User> users = Collections.singletonList(testUser);
        when(userRepository.findAll(Sort.by("name"))).thenReturn(users);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        // When
        List<UserDto> result = userService.getAllUsers("name");

        // Then
        assertEquals(1, result.size());
        assertEquals(testUserDto, result.get(0));
        verify(userRepository).findAll(Sort.by("name"));
        verify(userMapper).toDto(testUser);
    }

    @Test
    void getAllUsers_WithInvalidSort_DefaultsToNameSort() {
        // Given
        List<User> users = Collections.singletonList(testUser);
        when(userRepository.findAll(Sort.by("name"))).thenReturn(users);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        // When
        List<UserDto> result = userService.getAllUsers("invalid");

        // Then
        assertEquals(1, result.size());
        verify(userRepository).findAll(Sort.by("name"));
    }

    @Test
    void getUserById_WithExistingUser_ReturnsUserDto() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        // When
        UserDto result = userService.getUserById(1L);

        // Then
        assertEquals(testUserDto, result);
        verify(userRepository).findById(1L);
        verify(userMapper).toDto(testUser);
    }

    @Test
    void getUserById_WithNonExistingUser_ThrowsUserNotFoundException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(1L));
        verify(userRepository).findById(1L);
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void createUser_WithValidRequest_ReturnsUserDto() {
        // Given
        User newUser = new User();
        newUser.setName("Jane Doe");
        newUser.setEmail("jane@example.com");
        newUser.setPassword("password123");

        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(userMapper.toEntity(registerRequest)).thenReturn(newUser);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");
        when(userMapper.toDto(newUser)).thenReturn(new UserDto(2L, "Jane Doe", "jane@example.com"));

        // When
        UserDto result = userService.createUser(registerRequest);

        // Then
        assertNotNull(result);
        assertEquals("Jane Doe", result.getName());
        assertEquals("jane@example.com", result.getEmail());
        verify(userRepository).existsByEmail("jane@example.com");
        verify(userMapper).toEntity(registerRequest);
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(newUser);
        verify(userMapper).toDto(newUser);
        assertEquals(Role.USER, newUser.getRole());
        assertEquals("encodedPassword123", newUser.getPassword());
    }

    @Test
    void createUser_WithExistingEmail_ThrowsEmailAlreadyExistsException() {
        // Given
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        // When & Then
        assertThrows(EmailAlreadyExistsException.class, () -> userService.createUser(registerRequest));
        verify(userRepository).existsByEmail("jane@example.com");
        verify(userMapper, never()).toEntity(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_WithExistingUser_ReturnsUpdatedUserDto() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        // When
        UserDto result = userService.updateUser(1L, updateRequest);

        // Then
        assertEquals(testUserDto, result);
        verify(userRepository).findById(1L);
        verify(userMapper).update(updateRequest, testUser);
        verify(userRepository).save(testUser);
        verify(userMapper).toDto(testUser);
    }

    @Test
    void updateUser_WithNonExistingUser_ThrowsUserNotFoundException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> userService.updateUser(1L, updateRequest));
        verify(userRepository).findById(1L);
        verify(userMapper, never()).update(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_WithExistingUser_DeletesUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository).findById(1L);
        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteUser_WithNonExistingUser_ThrowsUserNotFoundException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(1L));
        verify(userRepository).findById(1L);
        verify(userRepository, never()).delete(any());
    }

    @Test
    void changePassword_WithValidCredentials_ChangesPassword() {
        // Given
        testUser.setPassword("oldPassword");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        userService.changePassword(1L, "oldPassword", "newPassword");

        // Then
        assertEquals("newPassword", testUser.getPassword());
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    void changePassword_WithInvalidOldPassword_ThrowsUserNotAuthorizedException() {
        // Given
        testUser.setPassword("oldPassword");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThrows(UserNotAuthorizedException.class, 
            () -> userService.changePassword(1L, "wrongPassword", "newPassword"));
        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_WithNonExistingUser_ThrowsUserNotFoundException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, 
            () -> userService.changePassword(1L, "oldPassword", "newPassword"));
        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any());
    }
}