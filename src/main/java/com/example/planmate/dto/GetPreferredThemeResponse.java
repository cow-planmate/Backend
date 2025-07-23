package com.example.planmate.dto;

import com.example.planmate.entity.PreferredTheme;
import com.example.planmate.valueObject.PreferredThemeVO;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class GetPreferredThemeResponse extends CommonResponse{
    private final List<PreferredThemeVO> preferredThemes;
    public GetPreferredThemeResponse(){
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
