package com.example.planmate.common.oauth.service;

import com.example.planmate.common.auth.JwtTokenProvider;
import com.example.planmate.common.oauth.dto.OAuthCompleteRequest;
import com.example.planmate.common.oauth.dto.OAuthCompleteResponse;
import com.example.planmate.domain.user.entity.User;
import com.example.planmate.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuthCompleteService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public OAuthCompleteResponse completeRegistration(OAuthCompleteRequest req) {

        User user = userRepository.findByProviderAndProviderId(
                req.getProvider(), req.getProviderId()
        ).orElseThrow(() -> new IllegalArgumentException("SNS 계정 정보를 찾을 수 없습니다"));

        // 이메일 업데이트 (카카오 null 대응)
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            user.changeEmail(req.getEmail());
        }

        // 나이/성별 업데이트
        user.changeAge(req.getAge());
        user.changeGender(req.getGender());

        userRepository.save(user);

        String access = jwtTokenProvider.generateAccessToken(user.getUserId());
        String refresh = jwtTokenProvider.generateRefreshToken(user.getUserId());

        return new OAuthCompleteResponse(
                true,
                "SNS 회원가입이 완료되었습니다",
                access,
                refresh,
                user.getUserId(),
                user.getNickname(),
                user.getEmail()
        );
    }
}
