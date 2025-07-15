package com.example.planmate.service;

import com.example.planmate.dto.ChangeAgeResponse;
import com.example.planmate.dto.ChangeGenderResponse;
import com.example.planmate.entity.User;
import com.example.planmate.exception.UserNotFoundException;
import com.example.planmate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangeGenderService {

    private final UserRepository userRepository;

    @Transactional
    public ChangeGenderResponse changeGender(int userId, int gender) {
        ChangeGenderResponse response = new ChangeGenderResponse();

        if (gender != 0 && gender != 1) {
            throw new IllegalArgumentException("gender 값은 0(남성) 또는 1(여성)이어야 합니다.");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("존재하지 않는 유저 ID입니다"));

        user.setGender(gender);

        response.setMessage("Gender changed successfully");

        return response;
    }
}
