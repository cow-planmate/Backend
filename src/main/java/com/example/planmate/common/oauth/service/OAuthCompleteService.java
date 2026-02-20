package com.example.planmate.common.oauth.service;

import com.example.planmate.common.auth.JwtTokenProvider;
import com.example.planmate.common.oauth.dto.OAuthCompleteRequest;
import com.example.planmate.common.oauth.dto.OAuthCompleteResponse;
import com.example.planmate.common.oauth.dto.OAuthSignupCache;
import com.example.planmate.domain.user.entity.User;
import com.example.planmate.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OAuthCompleteService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, OAuthSignupCache> oauthSignupRedis; // ✅ SNS 가입 임시 저장용

    @Transactional
    public OAuthCompleteResponse completeRegistration(OAuthCompleteRequest req) {

        // 1️⃣ Redis에서 가입 세션 조회
        String key = "signup:" + req.getSignupId();

        OAuthSignupCache cache =
                oauthSignupRedis.opsForValue().get(key);

        if (cache == null) {
            throw new IllegalArgumentException("가입 세션이 만료되었습니다.");
        }

        String finalEmail =
                (req.getEmail() != null && !req.getEmail().isBlank())
                        ? req.getEmail()
                        : cache.getEmail();

        if (finalEmail == null || finalEmail.isBlank()) {
            throw new IllegalArgumentException("이메일 정보가 필요합니다.");
        }


        // 2️⃣ 중복 가입 방지 체크
        if (userRepository.existsByProviderAndProviderId(
                cache.getProvider(), cache.getProviderId())) {
            throw new IllegalStateException("이미 가입된 계정입니다.");
        }

        // 3️⃣ 최초 User 생성
        User user = User.builder()
                .provider(cache.getProvider())
                .providerId(cache.getProviderId())
                .email(
                        (req.getEmail() != null && !req.getEmail().isBlank())
                                ? req.getEmail()
                                : cache.getEmail()
                )
                .nickname(cache.getNickname())
                .password(null)
                .age(req.getAge())
                .gender(req.getGender())
                .build();

        userRepository.save(user);

        // 4️⃣ Redis 삭제 (재사용 방지)
        oauthSignupRedis.delete(key);

        // 5️⃣ JWT 발급
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
