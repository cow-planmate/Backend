package com.example.planmate.common.oauth.service;

import com.example.planmate.common.oauth.config.OAuthProperties;
import com.example.planmate.common.oauth.dto.OAuthSignupCache;
import com.example.planmate.common.oauth.dto.OAuthUserProfile;
import com.example.planmate.common.oauth.dto.google.GoogleTokenResponse;
import com.example.planmate.common.oauth.dto.google.GoogleUserResponse;
import com.example.planmate.common.oauth.dto.naver.NaverTokenResponse;
import com.example.planmate.common.oauth.dto.naver.NaverUserResponse;
import com.example.planmate.common.oauth.enums.OAuthProvider;
import com.example.planmate.domain.user.entity.User;
import com.example.planmate.domain.user.repository.UserRepository;
import com.example.planmate.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuthLoginService {

    private final OAuthProperties oAuthProperties;
    private final UserService userService;
    private final UserRepository userRepository;
    private final OAuthCodeService oauthCodeService; // ✅ 추가

    private final RestTemplate restTemplate = new RestTemplate();
    private final RedisTemplate<String, OAuthSignupCache> oauthSignupRedis;

    /** 1. 로그인 시작 URL 생성 */
    public String buildAuthorizeUrl(OAuthProvider provider) {
        OAuthProperties.Provider config = oAuthProperties.getProvider(provider);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(config.getAuthorizeUri())
                .queryParam("client_id", config.getClientId())
                .queryParam("redirect_uri", config.getRedirectUri())
                .queryParam("response_type", "code");

        switch (provider) {
            case GOOGLE -> builder.queryParam("access_type", "offline")
                    .queryParam("prompt", "consent");
            case NAVER -> builder.queryParam("state", "RANDOM_STATE");
        }

        if (config.getScope() != null) {
            builder.queryParam("scope", config.getScope());
        }

        return builder.toUriString();
    }

    /** 2. callback 처리 */
    @Transactional
    public String handleCallback(OAuthProvider provider, String code, String state) {

        // 1) OAuth 프로필 가져오기
        OAuthUserProfile profile =
                switch (provider) {
                    case GOOGLE -> fetchGoogleProfile(code);
                    case NAVER -> fetchNaverProfile(code, state);
                };

        String providerName = provider.name().toLowerCase();
        String email = profile.getEmail();
        boolean needEmail = (email == null || email.isBlank());
        String providerId = profile.getProviderId();

        // 2) 이미 SNS로 가입된 유저인지 체크
        Optional<User> existedSNSUser =
                userRepository.findByProviderAndProviderId(providerName, providerId);

        if (existedSNSUser.isPresent()) {
            User user = existedSNSUser.get();

            // ✅ JWT 발급 ❌ → 1회용 loginCode 발급 ⭕
            String loginCode = oauthCodeService.issueLoginCode(user.getUserId());

            return buildLoginCodeRedirect(loginCode);
        }

        // 3) 이메일 충돌 체크 (local ↔ sns)
        if (email != null) {
            Optional<User> existing = userRepository.findByEmailIgnoreCase(email);

            if (existing.isPresent() && !existing.get().getProvider().equals(providerName)) {
                return buildFailRedirect();
            }
        }

        // 4) 신규 SNS 유저 → Redis에 임시 저장
        String sanitized = userService.sanitizeNickname(profile.getNickname());
        String finalNickname = userService.resolveUniqueNickname(sanitized);

        String signupId = UUID.randomUUID().toString();

        OAuthSignupCache cache = new OAuthSignupCache(
                providerName,
                providerId,
                email,
                finalNickname
        );

        oauthSignupRedis.opsForValue().set(
                "signup:" + signupId,
                cache,
                Duration.ofMinutes(15)
        );

        // 5) 추가 정보 입력 필요 (signupId만 전달)
        return buildAdditionalInfoRedirect(signupId, needEmail);

    }

    /** 기존 SNS 유저 로그인 → loginCode redirect */
    private String buildLoginCodeRedirect(String code) {
        return UriComponentsBuilder
                .fromUriString(oAuthProperties.getFrontendRedirectUri())
                .queryParam("status", "SUCCESS")
                .queryParam("code", code)
                .build(true)
                .toUriString();
    }

    /** 신규 SNS 유저 → 추가정보 입력 redirect */
    private String buildAdditionalInfoRedirect(String signupId, boolean needEmail) {
        return UriComponentsBuilder
                .fromUriString(oAuthProperties.getFrontendRedirectUri())
                .queryParam("status", "NEED_ADDITIONAL_INFO")
                .queryParam("signupId", signupId)
                .queryParam("needEmail", needEmail)
                .build(true)
                .toUriString();
    }

    private String buildFailRedirect() {
        return UriComponentsBuilder
                .fromUriString(oAuthProperties.getFrontendRedirectUri())
                .queryParam("status", "FAIL")
                .queryParam("reason", "EMAIL_CONFLICT")
                .build(true)
                .toUriString();
    }


    /* ================= PROVIDER 구현부 ================= */

    private OAuthUserProfile fetchGoogleProfile(String code) {
        OAuthProperties.Provider config = oAuthProperties.getProvider(OAuthProvider.GOOGLE);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", config.getClientId());
        body.add("client_secret", config.getClientSecret());
        body.add("redirect_uri", config.getRedirectUri());
        body.add("code", code);

        GoogleTokenResponse token = restTemplate.postForObject(
                config.getTokenUri(),
                new HttpEntity<>(body, headers),
                GoogleTokenResponse.class
        );

        HttpHeaders infoHeaders = new HttpHeaders();
        infoHeaders.setBearerAuth(token.getAccessToken());

        ResponseEntity<GoogleUserResponse> response = restTemplate.exchange(
                config.getUserInfoUri(),
                HttpMethod.GET,
                new HttpEntity<>(infoHeaders),
                GoogleUserResponse.class
        );

        GoogleUserResponse d = response.getBody();

        return new OAuthUserProfile(
                d.getSub(),
                d.getEmail(),
                d.getName()
        );
    }

    private OAuthUserProfile fetchNaverProfile(String code, String state) {
        OAuthProperties.Provider config = oAuthProperties.getProvider(OAuthProvider.NAVER);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", config.getClientId());
        body.add("client_secret", config.getClientSecret());
        body.add("redirect_uri", config.getRedirectUri());
        body.add("code", code);
        body.add("state", state);

        NaverTokenResponse token = restTemplate.postForObject(
                config.getTokenUri(),
                new HttpEntity<>(body, headers),
                NaverTokenResponse.class
        );

        HttpHeaders infoHeaders = new HttpHeaders();
        infoHeaders.setBearerAuth(token.getAccessToken());

        ResponseEntity<NaverUserResponse> response = restTemplate.exchange(
                config.getUserInfoUri(),
                HttpMethod.GET,
                new HttpEntity<>(infoHeaders),
                NaverUserResponse.class
        );

        NaverUserResponse.Result r = response.getBody().getResponse();

        return new OAuthUserProfile(
                r.getId(),
                r.getEmail(),
                r.getNickname()
        );
    }
}
