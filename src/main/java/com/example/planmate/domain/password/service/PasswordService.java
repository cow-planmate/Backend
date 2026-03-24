package com.example.planmate.domain.password.service;

import java.security.SecureRandom;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.planmate.common.exception.UserNotFoundException;
import com.example.planmate.domain.emailVerification.service.CustomMailService;
import com.example.planmate.domain.password.dto.ChangePasswordResponse;
import com.example.planmate.domain.password.dto.SendTempPasswordResponse;
import com.example.planmate.domain.password.dto.VerifyPasswordResponse;
import com.example.planmate.domain.user.entity.User;
import com.example.planmate.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom;
    private final CustomMailService customMailService;

    public VerifyPasswordResponse verifyPassword(UUID userId, String password) {
        VerifyPasswordResponse response = new VerifyPasswordResponse();

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            response.setMessage("현재 비밀번호가 일치하지 않습니다.");
            response.setPasswordVerified(false);
        } else {
            response.setMessage("비밀번호가 일치합니다.");
            response.setPasswordVerified(true);
        }
        return response;
    }

    @Transactional
    public ChangePasswordResponse changePassword(UUID userId, String currentPassword, String newPassword, String confirmPassword) {
        ChangePasswordResponse response = new ChangePasswordResponse();

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        } else{
            String encodedPassword = passwordEncoder.encode(newPassword);
            user.changePassword(encodedPassword);
            userRepository.save(user);

            response.setMessage("비밀번호가 성공적으로 변경되었습니다.");
        }
        return response;
    }

    @Transactional
    public SendTempPasswordResponse sendTempPassword(String email) {
        SendTempPasswordResponse response = new SendTempPasswordResponse();
        //이메일 인증 토큰 처리 과정 필요
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(UserNotFoundException::new);

        String tempPassword = generateTempPassword();

        user.changePassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);

        customMailService.sendTemporaryPasswordMail(email, tempPassword);

        response.setMessage("임시 비밀번호를 전송했습니다");

        return response;
    }

    private String generateTempPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(secureRandom.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
