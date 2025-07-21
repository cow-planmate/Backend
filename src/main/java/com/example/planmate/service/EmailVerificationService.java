package com.example.planmate.service;

import com.example.planmate.dto.SendEmailResponse;
import com.example.planmate.dto.EmailVerificationResponse;
import com.example.planmate.gita.EmailVerification;
import com.example.planmate.repository.UserRepository;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private final UserRepository userRepository;
    private final Cache<String, EmailVerification> verificationCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    public SendEmailResponse sendVerificationCode(String email) {
        SendEmailResponse response = new SendEmailResponse();
        int code = 12345; // 예: "123456"
        EmailVerification verification = new EmailVerification(email, code);
        verificationCache.put(email, verification);
        // 여기서 이메일 전송 로직 추가
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
        response.setEmailVerified(true);
        return response;
    }
}
