package com.example.planmate.common.valueObject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "선호 테마 카테고리별 업데이트 정보")
public class PreferredThemeUpdateVO {
    @Schema(description = "선호 테마 카테고리 ID (0:관광지, 1:식당, 2:숙소)", example = "1")
    private int preferredThemeCategoryId;

    @Schema(description = "카테고리에 속하는 새 선호 테마 ID 목록", example = "[1, 2, 3]")
    private List<Integer> preferredThemeIds;
}
