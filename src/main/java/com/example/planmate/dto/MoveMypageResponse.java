package com.example.planmate.dto;

import com.example.planmate.entity.PreferredTheme;
import com.example.planmate.valueObject.PreferredThemeVO;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MoveMypageResponse extends CommonResponse {
    private int userId;
    private String email;
    private String nickname;
    private int age;
    private int gender;
    private List<String> planNames;
    @Setter(AccessLevel.NONE)
    private List<PreferredThemeVO> preferredThemes;

    public MoveMypageResponse(){
        preferredThemes= new ArrayList<>();
    }
    public void addPreferredTheme(PreferredTheme preferredTheme) {
        PreferredThemeVO preferredThemeVO = new PreferredThemeVO(
                preferredTheme.getPreferredThemeId(),
                preferredTheme.getPreferredThemeName(),
                preferredTheme.getPreferredThemeCategory().getPreferredThemeCategoryId(),
                preferredTheme.getPreferredThemeCategory().getPreferredThemeCategoryName());
        preferredThemes.add(preferredThemeVO);
    }
}
