package com.example.planmate.generated.lazydto;

import com.example.planmate.domain.user.entity.User;
import com.sharedsync.framework.shared.framework.annotation.CacheEntity;
import com.sharedsync.framework.shared.framework.annotation.CacheId;
import com.sharedsync.framework.shared.framework.annotation.EntityConverter;
import com.sharedsync.framework.shared.framework.dto.CacheDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@CacheEntity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserDto extends CacheDto<Integer> {
    @CacheId
    private Integer userId;
    private String email;
    private String password;
    private String nickname;
    private int age;
    private int gender;

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
    @EntityConverter
    public User toEntity() {
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