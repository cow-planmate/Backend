package com.example.planmate.domain.password.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.planmate.domain.emailVerificaiton.service.CustomMailService;
import com.example.planmate.domain.password.dto.ChangePasswordResponse;
import com.example.planmate.domain.password.dto.SendTempPasswordResponse;
import com.example.planmate.domain.password.dto.VerifyPasswordResponse;
import com.example.planmate.domain.user.entity.User;
import com.example.planmate.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class PasswordServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SecureRandom secureRandom;
    @Mock
    private CustomMailService customMailService;

    @InjectMocks
    private PasswordService passwordService;

    @Test
    @DisplayName("verifyPassword: 비밀번호가 일치하면 인증 성공을 반환한다.")
    void verifyPassword_success() {
        // given
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        given(user.getPassword()).willReturn("encodedPassword");
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);

        // when
        VerifyPasswordResponse response = passwordService.verifyPassword(userId, "password123");

        // then
        assertTrue(response.isPasswordVerified());
    }

    @Test
    @DisplayName("changePassword: 비밀번호 변경에 성공한다.")
    void changePassword_success() {
        // given
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        given(user.getPassword()).willReturn("encodedPassword");
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("currentPassword", "encodedPassword")).willReturn(true);
        given(passwordEncoder.encode("newPassword")).willReturn("newEncodedPassword");

        // when
        ChangePasswordResponse response = passwordService.changePassword(userId, "currentPassword", "newPassword",
                "newPassword");

        // then
        assertEquals("비밀번호가 성공적으로 변경되었습니다.", response.getMessage());
        verify(user).changePassword("newEncodedPassword");
    }

    @Test
    @DisplayName("sendTempPassword: 임시 비밀번호를 메일로 발송한다.")
    void sendTempPassword_success() {
        // given
        String email = "test@example.com";
        User user = mock(User.class);
        given(userRepository.findByEmailIgnoreCase(email)).willReturn(Optional.of(user));
        given(secureRandom.nextInt(anyInt())).willReturn(5); // mock random
        given(passwordEncoder.encode(anyString())).willReturn("encodedTempPassword");

        // when
        SendTempPasswordResponse response = passwordService.sendTempPassword(email);

        // then
        assertEquals("임시 비밀번호를 전송했습니다", response.getMessage());
        verify(userRepository).save(user);
        verify(customMailService).sendSimpleMail(eq(email), anyString(), anyString());
    }
}
