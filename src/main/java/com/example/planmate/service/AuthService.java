package com.example.planmate.service;

import com.example.planmate.auth.JwtTokenProvider;
import com.example.planmate.dto.*;
import com.example.planmate.entity.User;
import com.example.planmate.exception.UserNotFoundException;
import com.example.planmate.gita.CustomUserDetails;
import com.example.planmate.gita.EmailVerification;
import com.example.planmate.repository.UserRepository;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final SecureRandom secureRandom;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final Cache<String, EmailVerification> verificationCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();

    public RegisterResponse register(RegisterRequest request) {
        RegisterResponse response = new RegisterResponse();
        if (userRepository.findByEmailIgnoreCase(request.getEmail()).isPresent()) {
            response.setMessage("Email already exists");
            return response;
        }
        if (userRepository.findByNickname(request.getNickname()).isPresent()) {
            response.setMessage("Username already exists");
            return response;
        }
        User user = User.builder()
                .email(request.getEmail())
                .nickname(request.getNickname())
                .password(passwordEncoder.encode(request.getPassword()))
                .gender(request.getGender())
                .age(request.getAge())
                .build();

        userRepository.save(user);
        response.setMessage("User registered successfully");
        response.setUserId(user.getUserId());
        return response;
    }

    @Transactional
    public SendTempPasswordResponse sendTempPassword(String email) {
        SendTempPasswordResponse response = new SendTempPasswordResponse();
        //이메일 인증 토큰 처리 과정 필요
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new UserNotFoundException("존재하지 않는 유저입니다"));

        String tempPassword = generateTempPassword();

        user.setPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("PlanMate 임시 비밀번호입니다.");
        message.setText("임시 비밀번호: " + tempPassword);

        mailSender.send(message);

        response.setMessage("Temp password sent");

        return response;
    }

    private String generateTempPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(secureRandom.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public VerifyPasswordResponse verifyPassword(int userId, String password) {
        VerifyPasswordResponse response = new VerifyPasswordResponse();

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("존재하지 않는 유저 ID입니다"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            response.setMessage("현재 비밀번호가 일치하지 않습니다.");
            response.setPasswordVerified(false);
        } else {
            response.setMessage("비밀번호가 일치합니다.");
            response.setPasswordVerified(true);
        }
        return response;
    }

    public LoginResponse login(String email, String password) {
        LoginResponse response = new LoginResponse();
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            password
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String token = jwtTokenProvider.generateToken(userDetails.getUserId());

            response.setToken(token);
            response.setUserId(userDetails.getUserId());
            response.setMessage("Login successful");
            response.setLoginSuccess(true);
            return response;
        } catch (AuthenticationException e) {
            response.setMessage("Invalid username or password");
            response.setLoginSuccess(false);
            return response;
        }
    }
    @Transactional
    public ChangePasswordResponse changePassword(int userId, String password, String confirmPassword) {
        ChangePasswordResponse response = new ChangePasswordResponse();

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("존재하지 않는 유저 ID입니다"));

        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        } else{
            String encodedPassword = passwordEncoder.encode(password);
            user.setPassword(encodedPassword);
            userRepository.save(user);

            response.setMessage("비밀번호가 성공적으로 변경되었습니다.");
        }
        return response;
    }
    public NicknameVerificationResponse verifyNickname(String nickname) {
        NicknameVerificationResponse response = new NicknameVerificationResponse();
        if(userRepository.findByNickname(nickname).isPresent()) {
            response.setMessage("Nickname already exists");
            response.setNicknameAvailable(false);
            return response;
        }
        response.setNicknameAvailable(true);
        return response;
    }

    public SendEmailResponse sendVerificationCode(String email) {
        SendEmailResponse response = new SendEmailResponse();

        //이메일 정규식 검증

        if(userRepository.findByEmailIgnoreCase(email).isPresent()) {
            response.setMessage("Email already in use");
            response.setVerificationSent(false);
            return response;
        }

        int code = secureRandom.nextInt(900000) + 100000;
        EmailVerification verification = new EmailVerification(email, code);
        verificationCache.put(email, verification);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("PlanMate 인증 코드입니다.");
        message.setText("인증 코드: " + code);

        mailSender.send(message);

        response.setMessage("Verification code sent");
        response.setVerificationSent(true);

        return response;
    }

    // 인증 확인
    public EmailVerificationResponse registerEmailVerify(String email, int inputCode) {
        EmailVerificationResponse response = new EmailVerificationResponse();
        EmailVerification emailVerification = verificationCache.getIfPresent(email);
        //인증시간이 끝났을때
        if(emailVerification == null) {
            response.setMessage("The verification time has expired");
            response.setEmailVerified(false);
            return response;
        }
        //이메일을 이미 사용중일 때
        if(userRepository.findByEmailIgnoreCase(email).isPresent()) {
            response.setMessage("Email already in use");
            response.setEmailVerified(false);
            return response;
        }
        if(!emailVerification.verify(inputCode)){
            response.setMessage("Invalid verification code");
            response.setEmailVerified(false);
            return response;
        }
        verificationCache.invalidate(email);
        response.setMessage("Verification completed successfully");
        response.setEmailVerified(true);
        return response;
    }
}
