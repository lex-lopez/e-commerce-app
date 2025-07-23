package com.alopez.store.users.mappers;

import com.alopez.store.users.dtos.RegisterUserRequest;
import com.alopez.store.users.dtos.UpdateUserRequest;
import com.alopez.store.users.dtos.UserDto;
import com.alopez.store.users.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
    User toEntity(RegisterUserRequest request);
    void update(UpdateUserRequest request, @MappingTarget User user);
}
