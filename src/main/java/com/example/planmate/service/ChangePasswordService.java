package com.example.planmate.service;

import com.example.planmate.dto.ChangePasswordResponse;
import com.example.planmate.dto.VerifyPasswordResponse;
import com.example.planmate.entity.User;
import com.example.planmate.exception.UserNotFoundException;
import com.example.planmate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangePasswordService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

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
}
