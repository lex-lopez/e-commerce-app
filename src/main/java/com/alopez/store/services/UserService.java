package com.alopez.store.services;

import com.alopez.store.dtos.RegisterUserRequest;
import com.alopez.store.dtos.UpdateUserRequest;
import com.alopez.store.dtos.UserDto;
import com.alopez.store.exceptions.EmailAlreadyExistsException;
import com.alopez.store.exceptions.UserNotAuthorizedException;
import com.alopez.store.exceptions.UserNotFoundException;
import com.alopez.store.mappers.UserMapper;
import com.alopez.store.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public List<UserDto> getAllUsers(String sort){
        if (!Set.of("name", "email").contains(sort)) {
            sort = "name";
        }
        return userRepository.findAll(Sort.by(sort))
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    public UserDto getUserById(Long id) {
        var user = userRepository.findById(id).orElse(null);
        if ( user == null ) {
            throw new UserNotFoundException();
        }

        return userMapper.toDto(user);
    }

    public UserDto createUser(RegisterUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException();
        }

        var user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        return userMapper.toDto(user);
    }

    public UserDto updateUser(Long id, UpdateUserRequest request) {
        var user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new UserNotFoundException();
        }

        userMapper.update(request, user);
        userRepository.save(user);

        return userMapper.toDto(user);
    }

    public void deleteUser(Long id) {
        var user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new UserNotFoundException();
        }
        userRepository.delete(user);
    }

    public void changePassword(Long id, String oldPassword, String newPassword) {
        var user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new UserNotFoundException();
        }

        if (!user.getPassword().equals(oldPassword)) {
            throw new UserNotAuthorizedException();
        }

        user.setPassword(newPassword);
        userRepository.save(user);
    }
}
