package com.example.planmate.valueObject;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PreferredThemeVO {
    private int preferredThemeId;
    private String preferredThemeName;
    private int preferredThemeCategoryId;
    private String preferredThemeCategoryName;
}
