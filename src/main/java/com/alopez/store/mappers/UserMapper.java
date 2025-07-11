package com.alopez.store.mappers;

import com.alopez.store.dtos.UserDto;
import com.alopez.store.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
}
