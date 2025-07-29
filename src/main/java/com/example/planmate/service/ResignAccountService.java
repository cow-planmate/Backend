package com.example.planmate.service;

import com.example.planmate.dto.ChangeGenderResponse;
import com.example.planmate.dto.ResignAccountResponse;
import com.example.planmate.entity.User;
import com.example.planmate.exception.UserNotFoundException;
import com.example.planmate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResignAccountService {
    private final UserRepository userRepository;

    @Transactional
    public ResignAccountResponse resignAccount(int userId) {
        ResignAccountResponse response = new ResignAccountResponse();

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("존재하지 않는 유저 ID입니다");
        }

        userRepository.deleteById(userId);

        response.setMessage("The account has been successfully deleted.");

        return response;
    }
}
