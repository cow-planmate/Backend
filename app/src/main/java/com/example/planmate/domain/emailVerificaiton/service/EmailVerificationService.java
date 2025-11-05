package com.example.planmate.domain.emailVerificaiton.service;

import com.example.planmate.common.auth.JwtTokenProvider;
import com.example.planmate.common.service.CustomMailService;
import com.example.planmate.common.enums.MailTemplate;
import com.example.planmate.domain.emailVerificaiton.dto.SendEmailResponse;
import com.example.planmate.domain.emailVerificaiton.dto.EmailVerificationResponse;
import com.example.planmate.domain.emailVerificaiton.EmailVerification;
import com.example.planmate.domain.emailVerificaiton.enums.EmailVerificationPurpose;
import com.example.planmate.domain.user.repository.UserRepository;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private final UserRepository userRepository;
    private final SecureRandom secureRandom;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomMailService customMailService;

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

        customMailService.sendSimpleMail(
                email,
                MailTemplate.VERIFICATION_CODE.getSubject(),
                MailTemplate.VERIFICATION_CODE.formatBody(String.valueOf(code))
        );

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

        String token = jwtTokenProvider.generateEmailToken(email, purpose);
        response.setMessage("Verification completed successfully");
        response.setEmailVerified(true);
        response.setToken(token);
        return response;
    }
}
