package com.example.planmate.service;

import com.example.planmate.dto.RegisterRequest;
import com.example.planmate.dto.RegisterResponse;
import com.example.planmate.entity.User;
import com.example.planmate.repository.PreferredThemeRepository;
import com.example.planmate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PreferredThemeRepository preferredThemeRepository;

    public RegisterResponse register(RegisterRequest request) {
        RegisterResponse response = new RegisterResponse();
        if (userRepository.findByEmailIgnoreCase(request.getEmail()).isPresent()) {
            response.setMessage("Email already exists");
            return response;
        }
        if (userRepository.findByNickname(request.getNickname()).isPresent()) {
            response.setMessage("Username already exists");
            return response;
        }
        User user = User.builder()
                .email(request.getEmail())
                .nickname(request.getNickname())
                .password(passwordEncoder.encode(request.getPassword()))
                .gender(request.getGender())
                .age(request.getAge())
                .build();

        userRepository.save(user);
        response.setMessage("User registered successfully");
        response.setUserId(user.getUserId());
        return response;
    }




}
