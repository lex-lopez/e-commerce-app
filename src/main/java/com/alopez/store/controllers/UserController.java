package com.alopez.store.controllers;

import com.alopez.store.dtos.*;
import com.alopez.store.exceptions.EmailAlreadyExistsException;
import com.alopez.store.exceptions.UserNotAuthorizedException;
import com.alopez.store.exceptions.UserNotFoundException;
import com.alopez.store.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Operations for users")
public class UserController {
    private final UserService userService;

    @GetMapping
    @Operation(summary = "Gets all users")
    public List<UserDto> getAllUsers(
            @Parameter(description = "Sort by name or email", example = "name")
            @RequestParam(name = "sort", required = false, defaultValue = "") String sort
    ) {
       return userService.getAllUsers(sort);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Gets a user by id")
    public ResponseEntity<UserDto> getUserById(
            @Parameter(description = "The id of the user", required = true)
            @PathVariable Long id
    ) {
        var userDto = userService.getUserById(id);
        return ResponseEntity.ok(userDto);
    }

    @PostMapping
    @Operation(summary = "Registers a new user")
    public ResponseEntity<UserDto> registerUser(
            @Valid @RequestBody RegisterUserRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        var userDto = userService.createUser(request);
        var uri = uriBuilder.path("/users/{id}").buildAndExpand(userDto.getId()).toUri();
        return ResponseEntity.created(uri).body(userDto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Updates a user")
    public ResponseEntity<UserDto> updateUser(
            @Parameter(description = "The id of the user", required = true)
            @PathVariable(name = "id") Long id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        var userDto = userService.updateUser(id, request);
        return ResponseEntity.ok(userDto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletes a user")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "The id of the user", required = true)
            @PathVariable Long id
    ) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/change-password")
    @Operation(summary = "Changes the password of a user")
    public ResponseEntity<Void> changePassword(
            @Parameter(description = "The id of the user", required = true)
            @PathVariable Long id,
            @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(id, request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorDto> handleUserNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto("User not found!"));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorDto> handleEmailAlreadyExists() {
        return ResponseEntity.badRequest().body(new ErrorDto("Email is already registered!"));
    }

    @ExceptionHandler(UserNotAuthorizedException.class)
    public ResponseEntity<ErrorDto> handleUserNotAuthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorDto("User not authorized!"));
    }

}
