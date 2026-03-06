package com.example.planmate.domain.register.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.planmate.domain.register.dto.NicknameVerificationResponse;
import com.example.planmate.domain.register.dto.RegisterRequest;
import com.example.planmate.domain.register.dto.RegisterResponse;
import com.example.planmate.domain.user.entity.User;
import com.example.planmate.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class RegisterServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterService registerService;

    @Test
    @DisplayName("register: 정상적으로 회원가입을 수행한다.")
    void register_success() {
        // given
        String email = "test@test.com";
        RegisterRequest request = new RegisterRequest();
        request.setNickname("nickname1");
        request.setPassword("password123");

        given(userRepository.findByEmailIgnoreCase(email)).willReturn(Optional.empty());
        given(userRepository.findByNickname("nickname1")).willReturn(Optional.empty());
        given(passwordEncoder.encode("password123")).willReturn("encodedPassword");

        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return user;
        });

        // when
        RegisterResponse response = registerService.register(email, request);

        // then
        assertEquals("User registered successfully", response.getMessage());
        assertTrue(response.isRegistered());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register: 이미 사용중인 이메일이면 거절 반환.")
    void register_emailExists() {
        // given
        String email = "test@test.com";
        RegisterRequest request = new RegisterRequest();

        given(userRepository.findByEmailIgnoreCase(email)).willReturn(Optional.of(mock(User.class)));

        // when
        RegisterResponse response = registerService.register(email, request);

        // then
        assertEquals("이미 회원가입 되어있는 이메일입니다", response.getMessage());
        assertFalse(response.isRegistered());
    }

    @Test
    @DisplayName("verifyNickname: 사용 가능한 닉네임일 경우")
    void verifyNickname_available() {
        // given
        String nickname = "uniqueNick";
        given(userRepository.findByNickname(nickname)).willReturn(Optional.empty());

        // when
        NicknameVerificationResponse response = registerService.verifyNickname(nickname);

        // then
        assertTrue(response.isNicknameAvailable());
    }
}
