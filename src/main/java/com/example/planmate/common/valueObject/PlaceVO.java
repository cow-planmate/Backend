package com.example.planmate.common.valueObject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "장소 검색 결과 정보")
public class PlaceVO {
    @Schema(description = "Google Place ID", example = "ChIJ...")
    private String placeId;

    @Schema(description = "카테고리 ID", example = "1")
    private int categoryId;

    @Schema(description = "장소 상세 URL", example = "https://maps.google.com/...")
    private String url;

    @Schema(description = "장소 이름", example = "박물관")
    private String name;

    @Schema(description = "포맷된 주소", example = "서울특별시 ...")
    private String formatted_address;

    @Schema(description = "평점", example = "4.2")
    private float rating;

    @Schema(description = "사진 URL", example = "https://...")
    private String photoUrl;

    @Schema(description = "경도 (X 좌표)", example = "126.9768967")
    private double xLocation;

    @Schema(description = "위도 (Y 좌표)", example = "37.5776087")
    private double yLocation;

    @Schema(description = "아이콘 URL", example = "https://...")
    private String iconUrl;

    @Schema(description = "사진 참조용 토큰", example = "AUphs9V...")
    private String photoReference; 

    public PlaceVO(String placeId, int categoryId, String url, String name, String formatted_address, float rating, String photoUrl, double xLocation, double yLocation, String iconUrl) {
        this.placeId = placeId;
        this.categoryId = categoryId;
        this.url = url;
        this.name = name;
        this.formatted_address = formatted_address;
        this.rating = rating;
        this.photoUrl = photoUrl;
        this.xLocation = xLocation;
        this.yLocation = yLocation;
        this.iconUrl = iconUrl;
    }
}

