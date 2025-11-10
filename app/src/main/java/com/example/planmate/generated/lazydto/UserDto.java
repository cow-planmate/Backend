package com.example.planmate.generated.lazydto;

import com.example.planmate.domain.user.entity.User;
import com.sharedsync.framework.shared.framework.annotation.CacheEntity;
import com.sharedsync.framework.shared.framework.annotation.CacheId;
import com.sharedsync.framework.shared.framework.dto.EntityBackedCacheDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@CacheEntity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserDto extends EntityBackedCacheDto<Integer, User> {
    @CacheId
    private Integer userId;
    private String email;
    private String password;
    private String nickname;
    private int age;
    private int gender;

    public static UserDto fromEntity(User user) {
        return instantiateFromEntity(user, UserDto.class);
    }
}