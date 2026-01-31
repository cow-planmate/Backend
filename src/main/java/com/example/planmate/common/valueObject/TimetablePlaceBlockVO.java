package com.example.planmate.common.valueObject;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "타임테이블에 배치된 장소 블록 정보")
public class TimetablePlaceBlockVO {
    @Schema(description = "타임테이블 ID", example = "1")
    private int timeTableId;

    @Schema(description = "타임테이블 장소 블록 ID", example = "1")
    private Integer timetablePlaceBlockId;

    @Schema(description = "장소 카테고리 ID", example = "1")
    private Integer placeCategoryId;

    @Schema(description = "장소 이름", example = "광화문 광장")
    private String placeName;

    @Schema(description = "장소 평점", example = "4.7")
    private Float placeRating;

    @Schema(description = "장소 주소", example = "서울특별시 종로구 세종대로 172")
    private String placeAddress;

    @Schema(description = "장소 상세 링크", example = "https://...")
    private String placeLink;

    @Schema(description = "장소 사진 URL", example = "https://...")
    private String photoUrl;

    @Schema(description = "장소 ID (Google Place ID 등)", example = "ChIJ...")
    @JsonProperty("placeId")
    @JsonAlias({ "placePhotoId" })
    private String placeId;

    @Schema(description = "방문 날짜", example = "2023-10-01")
    private String date;

    @Schema(description = "방문 시작 시간", example = "13:00:00")
    @JsonProperty("startTime")
    @JsonAlias({ "blockStartTime" })    
    private LocalTime startTime;

    @Schema(description = "방문 종료 시간", example = "14:00:00")
    @JsonProperty("endTime")
    @JsonAlias({ "blockEndTime" })    
    private LocalTime endTime;

    @Schema(description = "경도 (X 좌표)", example = "126.9768967")
    @JsonProperty("xLocation")
    private Double xLocation;

    @Schema(description = "위도 (Y 좌표)", example = "37.5776087")
    @JsonProperty("yLocation")
    private Double yLocation;
}
