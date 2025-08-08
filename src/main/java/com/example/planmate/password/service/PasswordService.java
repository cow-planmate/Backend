package com.example.planmate.password.service;

import com.example.planmate.password.dto.ChangePasswordResponse;
import com.example.planmate.password.dto.SendTempPasswordResponse;
import com.example.planmate.password.dto.VerifyPasswordResponse;
import com.example.planmate.entity.User;
import com.example.planmate.exception.UserNotFoundException;
import com.example.planmate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class PasswordService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final SecureRandom secureRandom;

    public VerifyPasswordResponse verifyPassword(int userId, String password) {
        VerifyPasswordResponse response = new VerifyPasswordResponse();

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("존재하지 않는 유저 ID입니다"));

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
    public ChangePasswordResponse changePassword(int userId, String password, String confirmPassword) {
        ChangePasswordResponse response = new ChangePasswordResponse();

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("존재하지 않는 유저 ID입니다"));

        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        } else{
            String encodedPassword = passwordEncoder.encode(password);
            user.setPassword(encodedPassword);
            userRepository.save(user);

            response.setMessage("비밀번호가 성공적으로 변경되었습니다.");
        }
        return response;
    }

    @Transactional
    public SendTempPasswordResponse sendTempPassword(String email) {
        SendTempPasswordResponse response = new SendTempPasswordResponse();
        //이메일 인증 토큰 처리 과정 필요
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new UserNotFoundException("존재하지 않는 유저입니다"));

        String tempPassword = generateTempPassword();

        user.setPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("PlanMate 임시 비밀번호입니다.");
        message.setText("임시 비밀번호: " + tempPassword);

        mailSender.send(message);

        response.setMessage("Temp password sent");

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
