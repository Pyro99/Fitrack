package com.fitness.userservice.mapper;

import com.fitness.userservice.dto.UserRequestDTO;
import com.fitness.userservice.dto.UserResponseDTO;
import com.fitness.userservice.model.User;


public class UserMapper {

    public static UserResponseDTO toDto(User user){
        UserResponseDTO userResponse = new UserResponseDTO();

        userResponse.setId(user.getId());
        userResponse.setEmail(user.getEmail());
        userResponse.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());

        return userResponse;
    };

    public static User toModel(UserRequestDTO registerRequest){
        User user = new User();

        user.setEmail(registerRequest.getEmail());
        user.setPassword(registerRequest.getPassword());
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());

        return user;
    }
}
//private String id;
//private String email;
//private String password;
//private String firstName;
//private String lastName;
//private LocalDateTime createdAt;
//private LocalDateTime updatedAt;