package com.example.planmate.common.valueObject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "선호 테마 정보")
public class PreferredThemeVO {
    @Schema(description = "선호 테마 ID", example = "1")
    private int preferredThemeId;

    @Schema(description = "선호 테마 이름", example = "박물관")
    private String preferredThemeName;

    @Schema(description = "선호 테마 카테고리 ID", example = "1")
    private int preferredThemeCategoryId;

    @Schema(description = "선호 테마 카테고리 이름", example = "문화/예술")
    private String preferredThemeCategoryName;
}
