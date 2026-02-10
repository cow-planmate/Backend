package com.example.planmate.domain.user.repository;

import com.example.planmate.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByNickname(String nickname);
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

}