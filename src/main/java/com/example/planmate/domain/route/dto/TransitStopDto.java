package com.example.planmate.domain.route.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "대중교통 구간의 경유 정류장/역 정보")
public class TransitStopDto {
    @Schema(description = "구간 내 순번 (ODsay index)")
    private Integer index;

    @Schema(description = "정류장/역 이름")
    private String stationName;

    @Schema(description = "경도 (ODsay가 문자열로 반환 → Double 변환)")
    private Double x;

    @Schema(description = "위도 (ODsay가 문자열로 반환 → Double 변환)")
    private Double y;

    @Schema(description = "정류장 ARS 번호 (버스 정류장만; 없으면 null)")
    private String arsID;
}
