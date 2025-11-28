package com.example.planmate.common.oauth.service;

import com.example.planmate.common.auth.JwtTokenProvider;
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
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

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

        if (config.getScope() != null)
            builder.queryParam("scope", config.getScope());

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

        // ⭐ 2) 이미 SNS로 가입된 유저인지 먼저 체크 (provider + providerId)
        Optional<User> existedSNSUser =
                userRepository.findByProviderAndProviderId(providerName, providerId);

        if (existedSNSUser.isPresent()) {
            User user = existedSNSUser.get();

            // 바로 JWT 발급 → 로그인
            String access = jwtTokenProvider.generateAccessToken(user.getUserId());
            String refresh = jwtTokenProvider.generateRefreshToken(user.getUserId());

            return buildFrontendRedirectUrl(access, refresh);
        }

        // ⭐ 3) 이메일 충돌 체크 (local ↔ sns 충돌)
        if (email != null) {
            Optional<User> existing = userRepository.findByEmailIgnoreCase(email);

            if (existing.isPresent() && !existing.get().getProvider().equals(providerName)) {
                throw new IllegalArgumentException(
                        "이미 해당 이메일로 가입된 계정이 있습니다. 같은 방식("
                                + existing.get().getProvider() + ")으로 로그인해주세요."
                );
            }
        }

        // ⭐ 4) 신규 SNS 유저 생성 (DB INSERT)
        String sanitized = userService.sanitizeNickname(profile.getNickname());
        String finalNickname = userService.resolveUniqueNickname(sanitized);

        User newUser = User.builder()
                .provider(providerName)
                .providerId(providerId)
                .email(email)              // null 가능 → complete에서 채움
                .nickname(finalNickname)
                .password(null)            // SNS는 패스워드 없음
                .age(null)
                .gender(null)
                .build();

        newUser = userRepository.save(newUser); // DB 저장

        // ⭐ 5) 추가 정보 입력 필요 → 프론트로 redirect
        // (닉네임/이메일/아이디 모두 프론트에서 표시)
        return buildAdditionalInfoRedirect(
                providerName,
                providerId,
                email,
                finalNickname
        );
    }




    /** 프론트에서 추가정보 입력하라고 보내는 redirect */
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
                .build(true)  // ★ 반드시 넣어야 함
                .toUriString();
    }




    /** 정상 로그인 redirect */
    private String buildFrontendRedirectUrl(String access, String refresh) {
        return UriComponentsBuilder
                .fromUriString(oAuthProperties.getFrontendRedirectUri())
                .queryParam("status", UriUtils.encode("SUCCESS", StandardCharsets.UTF_8))
                .queryParam("access", UriUtils.encode(access, StandardCharsets.UTF_8))
                .queryParam("refresh", UriUtils.encode(refresh, StandardCharsets.UTF_8))
                .build(true)
                .toUriString();
    }




    /** =========== PROVIDER 구현부 =========== */

    private OAuthUserProfile fetchKakaoProfile(String code) {
        OAuthProperties.Provider config = oAuthProperties.getProvider(OAuthProvider.KAKAO);

        // token 요청
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

        // user info 요청
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
