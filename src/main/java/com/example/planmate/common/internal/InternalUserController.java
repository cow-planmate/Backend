package com.example.planmate.common.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.planmate.domain.user.repository.UserRepository;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;

@Hidden
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/internal")
public class InternalUserController {

    private final UserRepository userRepository;

    @GetMapping("/users")
    public ResponseEntity<List<InternalUserResponse>> getUsers(@RequestParam("ids") String ids) {
        if (ids == null || ids.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        List<UUID> userIds = new ArrayList<>();
        try {
            for (String id : ids.split(",")) {
                userIds.add(UUID.fromString(id.trim()));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        List<InternalUserResponse> response = userRepository.findAllById(userIds).stream()
                .map(user -> new InternalUserResponse(user.getUserId(), user.getNickname()))
                .toList();
        return ResponseEntity.ok(response);
    }
}
