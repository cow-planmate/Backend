package com.example.planmate.service;

import com.example.planmate.dto.VerifyPasswordResponse;
import com.example.planmate.entity.User;
import com.example.planmate.exception.UserNotFoundException;
import com.example.planmate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerifyPasswordService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public VerifyPasswordResponse verifyPassword(int userId, String password) {
        VerifyPasswordResponse response = new VerifyPasswordResponse();

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("존재하지 않는 유저 ID입니다"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            response.setMessage("현재 비밀번호가 일치하지 않습니다.");
            response.setPasswordVerified(false);
        }
        else {
            response.setMessage("비밀번호가 일치합니다.");
            response.setPasswordVerified(true);
        }
        return response;
    }
}
