package com.financeapp.mapper;

import com.financeapp.dto.response.UserResponse;
import com.financeapp.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);
}
