package com.alopez.store.mappers;

import com.alopez.store.dtos.RegisterUserRequest;
import com.alopez.store.dtos.UpdateUserRequest;
import com.alopez.store.dtos.UserDto;
import com.alopez.store.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
    User toEntity(RegisterUserRequest request);
    void update(UpdateUserRequest request, @MappingTarget User user);
}
