package com.example.planmate.service;

import com.example.planmate.dto.ChangeAgeResponse;
import com.example.planmate.dto.MoveMypageResponse;
import com.example.planmate.entity.PreferredTheme;
import com.example.planmate.entity.User;
import com.example.planmate.exception.UserNotFoundException;
import com.example.planmate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChangeAgeService {

    private final UserRepository userRepository;

    @Transactional
    public ChangeAgeResponse changeAge(int userId, int age) {
        ChangeAgeResponse response = new ChangeAgeResponse();

        if (age < 0) {
            throw new IllegalArgumentException("나이는 0 이상의 정수여야 합니다.");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("존재하지 않는 유저 ID입니다"));

        user.setAge(age);

        response.setMessage("Age changed successfully");

        return response;
    }
}
