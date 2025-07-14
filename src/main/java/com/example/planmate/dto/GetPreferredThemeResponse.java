package com.example.planmate.dto;

import com.example.planmate.entity.PreferredTheme;
import com.example.planmate.valueObject.PreferredThemeVO;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class GetPreferredThemeResponse extends CommonResponse{
    private List<PreferredThemeVO> preferredThemes;
    public GetPreferredThemeResponse(){
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
