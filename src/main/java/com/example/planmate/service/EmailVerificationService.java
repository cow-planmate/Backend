package com.example.planmate.service;

import com.example.planmate.dto.SendEmailResponse;
import com.example.planmate.dto.EmailVerificationResponse;
import com.example.planmate.gita.EmailVerification;
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

    private final Cache<String, EmailVerification> verificationCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    public SendEmailResponse sendVerificationCode(String email) {
        SendEmailResponse response = new SendEmailResponse();

        //이메일 정규식 검증

        if(userRepository.findByEmailIgnoreCase(email).isPresent()) {
            response.setMessage("Email already in use");
            response.setVerificationSent(false);
            return response;
        }

        int code = secureRandom.nextInt(900000) + 100000;
        EmailVerification verification = new EmailVerification(email, code);
        verificationCache.put(email, verification);

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
    public EmailVerificationResponse registerEmailVerify(String email, int inputCode) {
        EmailVerificationResponse response = new EmailVerificationResponse();
        EmailVerification emailVerification = verificationCache.getIfPresent(email);
        //인증시간이 끝났을때
        if(emailVerification == null) {
            response.setMessage("The verification time has expired");
            response.setEmailVerified(false);
            return response;
        }
        //이메일을 이미 사용중일 때
        if(userRepository.findByEmailIgnoreCase(email).isPresent()) {
            response.setMessage("Email already in use");
            response.setEmailVerified(false);
            return response;
        }
        if(!emailVerification.verify(inputCode)){
            response.setMessage("Invalid verification code");
            response.setEmailVerified(false);
            return response;
        }
        verificationCache.invalidate(email);
        response.setMessage("Verification completed successfully");
        response.setEmailVerified(true);
        return response;
    }
}
