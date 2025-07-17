package com.example.planmate.service;

import com.example.planmate.dto.MoveMypageResponse;
import com.example.planmate.dto.RegisterRequest;
import com.example.planmate.dto.RegisterResponse;
import com.example.planmate.entity.PreferredTheme;
import com.example.planmate.entity.User;
import com.example.planmate.exception.UserNotFoundException;
import com.example.planmate.repository.PreferredThemeRepository;
import com.example.planmate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MoveMypageService {

    private final UserRepository userRepository;

    public MoveMypageResponse getMypageInfo(int userId) {
        MoveMypageResponse response = new MoveMypageResponse();

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("존재하지 않는 유저 ID입니다"));

        List<PreferredTheme> preferredThemes = new ArrayList<>(user.getPreferredThemes());
        for (PreferredTheme preferredTheme : preferredThemes) {
            response.addPreferredTheme(preferredTheme);
        }

        response.setMessage("Mypage info loaded successfully");
        response.setUserId(user.getUserId());
        response.setEmail(user.getEmail());
        response.setNickname(user.getNickname());
        response.setAge(user.getAge());
        response.setGender(user.getGender());

        return response;
    }
}
