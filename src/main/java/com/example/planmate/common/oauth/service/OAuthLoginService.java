package com.example.planmate.common.oauth.service;

import com.example.planmate.common.oauth.config.OAuthProperties;
import com.example.planmate.common.oauth.dto.OAuthUserProfile;
import com.example.planmate.common.oauth.dto.google.GoogleTokenResponse;
import com.example.planmate.common.oauth.dto.google.GoogleUserResponse;
import com.example.planmate.common.oauth.dto.kakao.KakaoTokenResponse;
import com.example.planmate.common.oauth.dto.kakao.KakaoUserResponse;
import com.example.planmate.common.oauth.dto.naver.NaverTokenResponse;
import com.example.planmate.common.oauth.dto.naver.NaverUserResponse;
import com.example.planmate.common.oauth.enums.OAuthProvider;
import com.example.planmate.domain.user.entity.User;
import com.example.planmate.domain.user.repository.UserRepository;
import com.example.planmate.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuthLoginService {

    private final OAuthProperties oAuthProperties;
    private final UserService userService;
    private final UserRepository userRepository;
    private final OAuthCodeService oauthCodeService; // ✅ 추가

    private final RestTemplate restTemplate = new RestTemplate();

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
                    case KAKAO -> fetchKakaoProfile(code);
                    case GOOGLE -> fetchGoogleProfile(code);
                    case NAVER -> fetchNaverProfile(code, state);
                };

        String providerName = provider.name().toLowerCase();
        String email = profile.getEmail();
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
                throw new IllegalArgumentException(
                        "이미 해당 이메일로 가입된 계정이 있습니다. 같은 방식("
                                + existing.get().getProvider() + ")으로 로그인해주세요."
                );
            }
        }

        // 4) 신규 SNS 유저 생성
        String sanitized = userService.sanitizeNickname(profile.getNickname());
        String finalNickname = userService.resolveUniqueNickname(sanitized);

        User newUser = User.builder()
                .provider(providerName)
                .providerId(providerId)
                .email(email)          // null 가능
                .nickname(finalNickname)
                .password(null)        // SNS는 패스워드 없음
                .age(null)
                .gender(null)
                .build();

        userRepository.save(newUser);

        // 5) 추가 정보 입력 필요
        return buildAdditionalInfoRedirect(
                providerName,
                providerId,
                email,
                finalNickname
        );
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
    private String buildAdditionalInfoRedirect(
            String provider,
            String providerId,
            String email,
            String nickname
    ) {
        return UriComponentsBuilder
                .fromUriString(oAuthProperties.getFrontendRedirectUri())
                .queryParam("status", UriUtils.encode("NEED_ADDITIONAL_INFO", StandardCharsets.UTF_8))
                .queryParam("provider", UriUtils.encode(provider, StandardCharsets.UTF_8))
                .queryParam("providerId", UriUtils.encode(providerId, StandardCharsets.UTF_8))
                .queryParam("email", UriUtils.encode(email == null ? "" : email, StandardCharsets.UTF_8))
                .queryParam("nickname", UriUtils.encode(nickname, StandardCharsets.UTF_8))
                .build(true)
                .toUriString();
    }

    /* ================= PROVIDER 구현부 ================= */

    private OAuthUserProfile fetchKakaoProfile(String code) {
        OAuthProperties.Provider config = oAuthProperties.getProvider(OAuthProvider.KAKAO);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", config.getClientId());
        body.add("client_secret", config.getClientSecret());
        body.add("redirect_uri", config.getRedirectUri());
        body.add("code", code);

        KakaoTokenResponse token = restTemplate.postForObject(
                config.getTokenUri(),
                new HttpEntity<>(body, headers),
                KakaoTokenResponse.class
        );

        HttpHeaders infoHeaders = new HttpHeaders();
        infoHeaders.setBearerAuth(token.getAccessToken());

        ResponseEntity<KakaoUserResponse> response = restTemplate.exchange(
                config.getUserInfoUri(),
                HttpMethod.GET,
                new HttpEntity<>(infoHeaders),
                KakaoUserResponse.class
        );

        KakaoUserResponse d = response.getBody();

        return new OAuthUserProfile(
                String.valueOf(d.getId()),
                d.getKakaoAccount().getEmail(),
                d.getKakaoAccount().getProfile().getNickname()
        );
    }

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
