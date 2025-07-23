package com.example.planmate.valueObject;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PreferredThemeVO {
    private int preferredThemeId;
    private String preferredThemeName;
    private int preferredThemeCategoryId;
    private String preferredThemeCategoryName;
}
