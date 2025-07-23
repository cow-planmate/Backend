package com.example.planmate.service;

import com.example.planmate.dto.SendTempPasswordResponse;
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
public class SendTempPasswordService {
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final SecureRandom secureRandom;
    private final PasswordEncoder passwordEncoder;

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
