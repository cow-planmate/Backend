package com.example.planmate.service;

import com.example.planmate.auth.JwtTokenProvider;
import com.example.planmate.dto.SendEmailResponse;
import com.example.planmate.dto.EmailVerificationResponse;
import com.example.planmate.gita.EmailVerification;
import com.example.planmate.gita.EmailVerificationPurpose;
import com.example.planmate.repository.UserRepository;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final SecureRandom secureRandom;
    private final JwtTokenProvider jwtTokenProvider;

    private final Cache<String, EmailVerification> verificationCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    public SendEmailResponse sendVerificationCode(String email, EmailVerificationPurpose purpose) {
        SendEmailResponse response = new SendEmailResponse();

        //이메일 정규식 검증

        if (EmailVerificationPurpose.SIGN_UP.equals(purpose)) {
            if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
                response.setMessage("Email already in use");
                response.setVerificationSent(false);
                return response;
            }
        } else if (EmailVerificationPurpose.RESET_PASSWORD.equals(purpose)) {
            if (userRepository.findByEmailIgnoreCase(email).isEmpty()) {
                response.setMessage("Email not found");
                response.setVerificationSent(false);
                return response;
            }
        }

        String cacheKey = email + "_" + purpose.name();
        int code = secureRandom.nextInt(900000) + 100000;
        EmailVerification verification = new EmailVerification(email, purpose, code);
        verificationCache.put(cacheKey, verification);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("PlanMate 인증 코드입니다.");
        message.setText("인증 코드: " + code);

        mailSender.send(message);

        response.setMessage("Verification code sent");
        response.setVerificationSent(true);

        return response;
    }

    // 인증 확인
    public EmailVerificationResponse registerEmailVerify(String email, EmailVerificationPurpose purpose, int inputCode) {
        EmailVerificationResponse response = new EmailVerificationResponse();
        String cacheKey = email + "_" + purpose.name();
        EmailVerification emailVerification = verificationCache.getIfPresent(cacheKey);

        if(emailVerification == null) {
            response.setMessage("Verification request not found or expired");
            response.setEmailVerified(false);
            return response;
        }

        if(!emailVerification.verify(purpose, inputCode)){
            response.setMessage("Invalid verification code");
            response.setEmailVerified(false);
            return response;
        }
        verificationCache.invalidate(cacheKey);

        String token = jwtTokenProvider.generateToken(email, purpose);
        response.setMessage("Verification completed successfully");
        response.setEmailVerified(true);
        response.setToken(token);
        return response;
    }
}
