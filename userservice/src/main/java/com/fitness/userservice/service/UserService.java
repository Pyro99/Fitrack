package com.fitness.userservice.service;

import com.fitness.userservice.dto.UserRequestDTO;
import com.fitness.userservice.dto.UserResponseDTO;
import com.fitness.userservice.mapper.UserMapper;
import com.fitness.userservice.model.User;
import com.fitness.userservice.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponseDTO register(UserRequestDTO requestDto) {

        if(userRepository.existsByEmail(requestDto.getEmail())){
            throw new RuntimeException("Email already exists!");
        }

        User user = UserMapper.toModel(requestDto);
        User savedUser = userRepository.save(user);
        return UserMapper.toDto(savedUser);
    }

    public UserResponseDTO getuserProfile(String userId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User with id: "+userId+" not found"));
        return UserMapper.toDto(user);
    }
}
