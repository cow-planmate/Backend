package com.example.planmate.generated.presence;

import com.example.planmate.domain.user.repository.UserRepository;
import com.example.planmate.move.shared.presence.core.UserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlanmateUserProvider implements UserProvider {

    private final UserRepository userRepository;

    @Override
    public String findNicknameByUserId(int userId) {
        return userRepository.findById(userId)
                .map(user -> user.getNickname())
                .orElse("Unknown");
    }
}
