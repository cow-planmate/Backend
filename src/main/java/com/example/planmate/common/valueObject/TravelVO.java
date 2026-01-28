package com.example.planmate.common.valueObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "여행지 정보")
public class TravelVO {
    @Schema(description = "여행지 ID", example = "1")
    private int travelId;

    @Schema(description = "여행지 이름", example = "서울특별시")
    private String travelName;

    @Schema(description = "여행 카테고리 ID", example = "1")
    private int travelCategoryId;

    @Schema(description = "여행 카테고리 이름", example = "중구")
    private String travelCategoryName;
}
