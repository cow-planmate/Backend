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
    @Setter(AccessLevel.NONE)
    private List<PreferredThemeVO> preferredThemes;

    public MoveMypageResponse(){
        preferredThemes= new ArrayList<>();
    }
    public void addPreferredTheme(PreferredTheme preferredTheme) {
        PreferredThemeVO preferredThemeVO = new PreferredThemeVO();
        preferredThemeVO.setPreferredThemeId(preferredTheme.getPreferredThemeId());
        preferredThemeVO.setPreferredThemeName(preferredTheme.getPreferredThemeName());
        preferredThemeVO.setPreferredThemeCategoryId(preferredTheme.getPreferredThemeCategory().getPreferredThemeCategoryId());
        preferredThemeVO.setPreferredThemeCategoryName(preferredTheme.getPreferredThemeCategory().getPreferredThemeCategoryName());
        preferredThemes.add(preferredThemeVO);
    }
}
