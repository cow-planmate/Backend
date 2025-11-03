package com.example.planmate.domain.shared.lazydto;

import com.example.planmate.domain.user.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
public class UserDto {

    private Integer userId;
    private String email;
    private String password;
    private String nickname;
    private int age;
    private int gender;

    public static UserDto fromEntity(User user) {
        return UserDto.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .password(user.getPassword())
                .nickname(user.getNickname())
                .age(user.getAge())
                .gender(user.getGender())
                .build();
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