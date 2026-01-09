package com.example.planmate.domain.register.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.planmate.domain.register.dto.NicknameVerificationResponse;
import com.example.planmate.domain.register.dto.RegisterRequest;
import com.example.planmate.domain.register.dto.RegisterResponse;
import com.example.planmate.domain.user.entity.User;
import com.example.planmate.domain.user.repository.UserRepository;
import com.example.planmate.domain.webSocket.service.PresenceTrackingService;

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
            response.setMessage("이미 회원가입 되어있는 이메일입니다");
            return response;
        }
        if (userRepository.findByNickname(request.getNickname()).isPresent()) {
            response.setMessage("이미 사용중인 닉네임입니다");
            return response;
        }
        User user = User.builder()
                .provider("local") // 추가됨
                .providerId(null)  // local 계정은 providerId 없음
                .email(email)
                .nickname(request.getNickname())
                .password(passwordEncoder.encode(request.getPassword()))
                .gender(request.getGender())
                .age(request.getAge())
                .build();


        userRepository.save(user);
        presenceTrackingService.insertNickname(user.getUserId(), user.getNickname());
        response.setMessage("성공적으로 회원가입하였습니다");
        response.setUserId(user.getUserId());
        return response;
    }

    public NicknameVerificationResponse verifyNickname(String nickname) {
        NicknameVerificationResponse response = new NicknameVerificationResponse();
        if(userRepository.findByNickname(nickname).isPresent()) {
            response.setMessage("이미 사용중인 닉네임입니다");
            response.setNicknameAvailable(false);
            return response;
        }
        response.setNicknameAvailable(true);
        return response;
    }
}
