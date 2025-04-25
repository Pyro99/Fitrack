package com.fitness.userservice.controller;


import com.fitness.userservice.dto.UserRequestDTO;
import com.fitness.userservice.dto.UserResponseDTO;
import com.fitness.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUserProfile(@PathVariable String userId){
        return ResponseEntity.ok(userService.getuserProfile(userId));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserRequestDTO requestDto){
        return ResponseEntity.ok(userService.register(requestDto));
    }
}
