package com.fitness.userservice.dto;

import lombok.Data;

@Data
public class UserResponseDTO {

    private String id;
    private String email;
    private String firstName;
    private String lastName;
}
