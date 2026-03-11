package com.example.planmate.domain.emailVerificaiton.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.example.planmate.common.auth.JwtTokenProvider;
import com.example.planmate.common.service.CustomMailService;
import com.example.planmate.domain.emailVerificaiton.EmailVerification;
import com.example.planmate.domain.emailVerificaiton.dto.EmailVerificationResponse;
import com.example.planmate.domain.emailVerificaiton.dto.SendEmailResponse;
import com.example.planmate.domain.emailVerificaiton.enums.EmailVerificationPurpose;
import com.example.planmate.domain.user.entity.User;
import com.example.planmate.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private SecureRandom secureRandom;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private CustomMailService customMailService;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ValueOperations<String, Object> valueOperations;

    private EmailVerificationService emailVerificationService;

    @BeforeEach
    void setUp() {
        emailVerificationService = new EmailVerificationService(
                userRepository, secureRandom, jwtTokenProvider, customMailService, redisTemplate);
        // lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("sendVerificationCode: 회원가입 용도, 이미 가입된 경우 거절")
    void sendVerificationCode_signup_duplicate() {
        // given
        String email = "test@example.com";
        given(userRepository.findByEmailIgnoreCase(email)).willReturn(Optional.of(mock(User.class)));

        // when
        SendEmailResponse response = emailVerificationService.sendVerificationCode(email,
                EmailVerificationPurpose.SIGN_UP);

        // then
        assertFalse(response.isVerificationSent());
        assertEquals("Email already in use", response.getMessage());
    }

        @Test
        @DisplayName("sendVerificationCode: 회원가입 용도, 인증번호 메일을 발송한다")
        void sendVerificationCode_signup_success() {
        String email = "test@example.com";
        int generatedCode = 123456;
        String cacheKey = "email_verification:" + email + "_SIGN_UP";

        given(userRepository.findByEmailIgnoreCase(email)).willReturn(Optional.empty());
        given(secureRandom.nextInt(900000)).willReturn(generatedCode - 100000);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        SendEmailResponse response = emailVerificationService.sendVerificationCode(email,
            EmailVerificationPurpose.SIGN_UP);

        assertTrue(response.isVerificationSent());
        assertEquals("Verification code sent", response.getMessage());
        verify(valueOperations).set(eq(cacheKey),
            argThat(verification -> verification instanceof EmailVerification emailVerification
                && email.equals(emailVerification.getEmail())
                && EmailVerificationPurpose.SIGN_UP.equals(emailVerification.getPurpose())
                && generatedCode == emailVerification.getCode()),
            eq(5L), eq(TimeUnit.MINUTES));
        verify(customMailService).sendVerificationCodeMail(
            eq(email),
            anyString(),
            anyString(),
            anyString(),
            eq(String.valueOf(generatedCode)));
        }

    @Test
    @DisplayName("registerEmailVerify: 인증코드가 일치하면 성공 토큰을 반환한다")
    void registerEmailVerify_success() {
        // given
        String email = "test@example.com";
        int inputCode = 123456;
        EmailVerificationPurpose purpose = EmailVerificationPurpose.SIGN_UP;
        String cacheKey = "email_verification:" + email + "_" + purpose.name();

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        EmailVerification verification = new EmailVerification(email, purpose, inputCode);
        given(valueOperations.get(cacheKey)).willReturn(verification);
        given(jwtTokenProvider.generateEmailToken(email, purpose)).willReturn("emailverifytoken");

        // when
        EmailVerificationResponse response = emailVerificationService.registerEmailVerify(email, purpose, inputCode);

        // then
        assertTrue(response.isEmailVerified());
        assertEquals("emailverifytoken", response.getToken());
        verify(redisTemplate).delete(cacheKey);
    }
}
