package com.example.planmate.domain.register.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.planmate.domain.register.dto.NicknameVerificationResponse;
import com.example.planmate.domain.register.dto.RegisterRequest;
import com.example.planmate.domain.register.dto.RegisterResponse;
import com.example.planmate.generated.service.PresenceTrackingService;
import com.example.planmate.domain.user.entity.User;
import com.example.planmate.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegisterService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PresenceTrackingService presenceTrackingService;

    public RegisterResponse register(String email, RegisterRequest request) {
        RegisterResponse response = new RegisterResponse();
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            response.setMessage("Email already exists");
            return response;
        }
        if (userRepository.findByNickname(request.getNickname()).isPresent()) {
            response.setMessage("Username already exists");
            return response;
        }
        User user = User.builder()
                .email(email)
                .nickname(request.getNickname())
                .password(passwordEncoder.encode(request.getPassword()))
                .gender(request.getGender())
                .age(request.getAge())
                .build();

        userRepository.save(user);
    presenceTrackingService.insertNickname(user.getUserId(), user.getNickname());
        response.setMessage("User registered successfully");
        response.setUserId(user.getUserId());
        return response;
    }

    public NicknameVerificationResponse verifyNickname(String nickname) {
        NicknameVerificationResponse response = new NicknameVerificationResponse();
        if(userRepository.findByNickname(nickname).isPresent()) {
            response.setMessage("이미 존재하는 닉네임입니다");
            response.setNicknameAvailable(false);
            return response;
        }
        response.setNicknameAvailable(true);
        return response;
    }




}
