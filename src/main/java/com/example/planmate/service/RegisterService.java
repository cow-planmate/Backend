package com.example.planmate.service;

import com.example.planmate.dto.RegisterRequest;
import com.example.planmate.dto.RegisterResponse;
import com.example.planmate.entity.User;
import com.example.planmate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service

@RequiredArgsConstructor
public class RegisterService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterResponse register(RegisterRequest request) {
        RegisterResponse registerResponse = new RegisterResponse();
        if (userRepository.findByEmailIgnoreCase(request.getEmail()).isPresent()) {
            registerResponse.setMessage("Email already exists");
            return registerResponse;
        }
        if (userRepository.findByNickname(request.getNickname()).isPresent()) {
            registerResponse.setMessage("Username already exists");
            return registerResponse;
        }

        User user = User.builder()
                .email(request.getEmail())
                .nickname(request.getNickname())
                .password(passwordEncoder.encode(request.getPassword()))
                .gender(request.getGender())
                .age(request.getAge())
                .build();


        userRepository.save(user);
        registerResponse.setMessage("User registered successfully");
        return registerResponse;
    }


}
