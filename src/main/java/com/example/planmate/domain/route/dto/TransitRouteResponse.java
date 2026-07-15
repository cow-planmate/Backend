package com.example.planmate.domain.route.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "대중교통 경로 응답 데이터 (여러 경로 옵션)")
public class TransitRouteResponse {
    @Schema(description = "대중교통 경로 조회 가능 여부")
    private boolean available;

    @Schema(description = "조회 불가 사유 (성공 시 null)")
    private String message;

    @Schema(description = "대중교통 경로 옵션 목록 (최적경로순, 최대 10건)")
    private List<TransitRouteOptionDto> routes;

    @Schema(description = "버스 전용 경로 총 개수 (ODsay result.busCount)")
    private Integer busCount;

    @Schema(description = "지하철 전용 경로 총 개수 (ODsay result.subwayCount)")
    private Integer subwayCount;

    @Schema(description = "버스+지하철 경로 총 개수 (ODsay result.subwayBusCount)")
    private Integer subwayBusCount;
}
