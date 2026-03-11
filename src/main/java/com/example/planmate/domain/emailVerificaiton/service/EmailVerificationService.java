package com.example.planmate.domain.emailVerificaiton.service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.planmate.common.auth.JwtTokenProvider;
import com.example.planmate.common.service.CustomMailService;
import com.example.planmate.domain.emailVerificaiton.EmailVerification;
import com.example.planmate.domain.emailVerificaiton.dto.EmailVerificationResponse;
import com.example.planmate.domain.emailVerificaiton.dto.SendEmailResponse;
import com.example.planmate.domain.emailVerificaiton.enums.EmailVerificationPurpose;
import com.example.planmate.domain.user.repository.UserRepository;

@Service
public class EmailVerificationService {
    private static final String SIGN_UP_SUBJECT = "planMate 회원가입 인증번호를 확인해주세요";
    private static final String RESET_PASSWORD_SUBJECT = "planMate 비밀번호 재설정 인증번호를 확인해주세요";
    private static final String SIGN_UP_HEADLINE = "인증번호가 도착했어요";
    private static final String RESET_PASSWORD_HEADLINE = "비밀번호 재설정을 위한 인증번호입니다";
    private static final String SIGN_UP_DESCRIPTION = "아래 6자리 인증번호를 회원가입 화면에 입력하면 이메일 인증이 완료됩니다.";
    private static final String RESET_PASSWORD_DESCRIPTION = "아래 6자리 인증번호를 입력하면 비밀번호 재설정 절차를 이어서 진행할 수 있습니다.";

    private final UserRepository userRepository;
    private final SecureRandom secureRandom;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomMailService customMailService;
    private final RedisTemplate<String, Object> redisTemplate;

    public EmailVerificationService(
            UserRepository userRepository,
            SecureRandom secureRandom,
            JwtTokenProvider jwtTokenProvider,
            CustomMailService customMailService,
            @Qualifier("emailVerificationRedis") RedisTemplate<String, Object> redisTemplate) {
        this.userRepository = userRepository;
        this.secureRandom = secureRandom;
        this.jwtTokenProvider = jwtTokenProvider;
        this.customMailService = customMailService;
        this.redisTemplate = redisTemplate;
    }

    private static final String REDIS_PREFIX = "email_verification:";

    public SendEmailResponse sendVerificationCode(String email, EmailVerificationPurpose purpose) {
        SendEmailResponse response = new SendEmailResponse();

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

        String cacheKey = REDIS_PREFIX + email + "_" + purpose.name();
        int code = secureRandom.nextInt(900000) + 100000;
        EmailVerification verification = new EmailVerification(email, purpose, code);

        redisTemplate.opsForValue().set(cacheKey, verification, 5, TimeUnit.MINUTES);

        customMailService.sendVerificationCodeMail(
            email,
            getVerificationSubject(purpose),
            getVerificationHeadline(purpose),
            getVerificationDescription(purpose),
            String.valueOf(code));

        response.setMessage("Verification code sent");
        response.setVerificationSent(true);

        return response;
    }

    public EmailVerificationResponse registerEmailVerify(String email, EmailVerificationPurpose purpose,
            int inputCode) {
        EmailVerificationResponse response = new EmailVerificationResponse();
        String cacheKey = REDIS_PREFIX + email + "_" + purpose.name();
        EmailVerification emailVerification = (EmailVerification) redisTemplate.opsForValue().get(cacheKey);

        if (emailVerification == null) {
            response.setMessage("Verification request not found or expired");
            response.setEmailVerified(false);
            return response;
        }

        if (!emailVerification.verify(purpose, inputCode)) {
            response.setMessage("Invalid verification code");
            response.setEmailVerified(false);
            return response;
        }
        redisTemplate.delete(cacheKey);

        String token = jwtTokenProvider.generateEmailToken(email, purpose);
        response.setMessage("Verification completed successfully");
        response.setEmailVerified(true);
        response.setToken(token);
        return response;
    }

    private String getVerificationSubject(EmailVerificationPurpose purpose) {
        return switch (purpose) {
            case SIGN_UP -> SIGN_UP_SUBJECT;
            case RESET_PASSWORD -> RESET_PASSWORD_SUBJECT;
        };
    }

    private String getVerificationHeadline(EmailVerificationPurpose purpose) {
        return switch (purpose) {
            case SIGN_UP -> SIGN_UP_HEADLINE;
            case RESET_PASSWORD -> RESET_PASSWORD_HEADLINE;
        };
    }

    private String getVerificationDescription(EmailVerificationPurpose purpose) {
        return switch (purpose) {
            case SIGN_UP -> SIGN_UP_DESCRIPTION;
            case RESET_PASSWORD -> RESET_PASSWORD_DESCRIPTION;
        };
    }
}
