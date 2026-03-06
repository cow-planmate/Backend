package com.example.planmate.domain.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.planmate.common.exception.ResourceConflictException;
import com.example.planmate.domain.plan.repository.PlanEditorRepository;
import com.example.planmate.domain.plan.repository.PlanRepository;
import com.example.planmate.domain.user.dto.ChangeNicknameResponse;
import com.example.planmate.domain.user.dto.ResignAccountResponse;
import com.example.planmate.domain.user.entity.User;
import com.example.planmate.domain.user.repository.PreferredThemeRepository;
import com.example.planmate.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private PreferredThemeRepository preferredThemeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PlanRepository planRepository;
    @Mock
    private PlanEditorRepository planEditorRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("changeNickname: 닉네임을 성공적으로 변경한다.")
    void changeNickname_success() {
        // given
        UUID userId = UUID.randomUUID();
        String newNickname = "newNickname";
        User user = mock(User.class);

        given(userRepository.findByNickname(newNickname)).willReturn(Optional.empty());
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        ChangeNicknameResponse response = userService.changeNickname(userId, newNickname);

        // then
        assertEquals("성공적으로 닉네임이 변경되었습니다", response.getMessage());
        verify(user).changeNickname(newNickname);
    }

    @Test
    @DisplayName("changeNickname: 이미 존재하는 닉네임이면 예외를 던진다.")
    void changeNickname_conflict() {
        // given
        UUID userId = UUID.randomUUID();
        String newNickname = "newNickname";

        given(userRepository.findByNickname(newNickname)).willReturn(Optional.of(mock(User.class)));

        // when & then
        assertThrows(ResourceConflictException.class, () -> {
            userService.changeNickname(userId, newNickname);
        });
    }

    @Test
    @DisplayName("resignAccount: 계정을 성공적으로 삭제한다.")
    void resignAccount_success() {
        // given
        UUID userId = UUID.randomUUID();
        given(userRepository.existsById(userId)).willReturn(true);

        // when
        ResignAccountResponse response = userService.resignAccount(userId);

        // then
        assertEquals("성공적으로 계정이 삭제되었습니다", response.getMessage());
        verify(userRepository).deleteById(userId);
    }

    @Test
    @DisplayName("sanitizeNickname: 닉네임 특수문자를 올바르게 제거한다.")
    void sanitizeNickname() {
        // given
        String input = "  Hello!@ Emoji😊 Test  ";

        // when
        String result = userService.sanitizeNickname(input);

        // then
        assertEquals("HelloEmojiTest", result);
    }
}
