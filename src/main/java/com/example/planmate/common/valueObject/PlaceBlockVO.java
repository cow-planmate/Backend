package com.example.planmate.common.valueObject;

import java.time.LocalTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "장소 블록 정보 (추천/검색 시 사용)")
public class PlaceBlockVO {
    @Schema(description = "장소 블록 ID", example = "1")
    private int blockId;

    @Schema(description = "타임테이블 ID", example = "1")
    private int timeTableId;

    @Schema(description = "장소 카테고리 ID", example = "1")
    private int placeCategoryId;

    @Schema(description = "장소 이름", example = "경복궁")
    private String placeName;

    @Schema(description = "장소 테마/카테고리 설명", example = "역사적인 명소")
    private String placeTheme;

    @Schema(description = "장소 평점", example = "4.5")
    private Float placeRating;

    @Schema(description = "장소 주소", example = "서울특별시 종로구 사직로 161")
    private String placeAddress;

    @Schema(description = "장소 상세 링크(Google maps 등)", example = "https://maps.google.com/...")
    private String placeLink;

    @Schema(description = "장소 사진 URL", example = "https://...")
    private String photoUrl;

    @Schema(description = "Google Place ID", example = "ChIJ...")
    private String placeId;

    @Schema(description = "경도 (X 좌표)", example = "126.9768967")
    private Double xLocation;

    @Schema(description = "위도 (Y 좌표)", example = "37.5776087")
    private Double yLocation;

    @Schema(description = "시작 시간", example = "10:00:00")
    private LocalTime blockStartTime;

    @Schema(description = "종료 시간", example = "12:00:00")
    private LocalTime blockEndTime;
}
