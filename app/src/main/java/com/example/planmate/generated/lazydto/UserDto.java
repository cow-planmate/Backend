package com.example.planmate.generated.lazydto;

import com.example.planmate.domain.user.entity.User;

public record UserDto(
        Integer userId,
        String email,
        String password,
        String nickname,
        int age,
        int gender
) {
    public static UserDto fromEntity(User user) {
        return new UserDto(
                user.getUserId(),
                user.getEmail(),
                user.getPassword(),
                user.getNickname(),
                user.getAge(),
                user.getGender()
        );
    }

    public User toEntity(String encodedPassword) {
        return User.builder()
                .userId(this.userId)
                .email(this.email)
                .password(this.password)
                .nickname(this.nickname)
                .age(this.age)
                .gender(this.gender)
                .build();
    }
}