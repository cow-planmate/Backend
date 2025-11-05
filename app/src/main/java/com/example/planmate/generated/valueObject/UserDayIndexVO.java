package com.example.planmate.generated.valueObject;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserDayIndexVO {
    private String nickname;
    private int dayIndex;
    public UserDayIndexVO(String nickname, int dayIndex) {
        this.nickname = nickname;
        changeDayIndex(dayIndex);
    }
    public void changeDayIndex(int dayIndex){
        if(dayIndex >= 0){
            this.dayIndex = dayIndex;
        }
    }

}
