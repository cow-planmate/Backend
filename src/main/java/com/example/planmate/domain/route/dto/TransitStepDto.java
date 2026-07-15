package com.example.planmate.domain.route.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "대중교통 경로의 구간별 상세 정보(노선/승하차 정류장 수준)")
public class TransitStepDto {
    @Schema(description = "이동 수단 (1=지하철, 2=버스, 3=도보)")
    private Integer trafficType;

    @Schema(description = "구간 소요 시간(분)")
    private Integer sectionTime;

    @Schema(description = "구간 이동 거리(m)")
    private Integer distance;

    @Schema(description = "정거장/역 수 (도보는 null)")
    private Integer stationCount;

    @Schema(description = "노선명 (버스=노선번호, 지하철=노선명, 도보=null)")
    private String laneName;

    @Schema(description = "버스 종류 코드 (버스만; 1=일반,2=좌석,3=마을,4=직행,5=공항,6=간선 등)")
    private Integer busType;

    @Schema(description = "지하철 노선 코드 (지하철만)")
    private Integer subwayCode;

    @Schema(description = "승차 정류장/역 이름 (도보는 null)")
    private String startName;

    @Schema(description = "하차 정류장/역 이름 (도보는 null)")
    private String endName;

    @Schema(description = "지하철 방면 (지하철만)")
    private String wayName;

    @Schema(description = "지하철 승차 출구 번호 (지하철만; ODsay가 문자열로 반환)")
    private String startExitNo;

    @Schema(description = "지하철 하차 출구 번호 (지하철만; ODsay가 문자열로 반환)")
    private String endExitNo;

    @Schema(description = "배차 간격(분) (버스/지하철)")
    private Integer intervalTime;

    @Schema(description = "버스 승차 정류장 ARS 번호 (버스만)")
    private String startArsID;

    @Schema(description = "버스 하차 정류장 ARS 번호 (버스만)")
    private String endArsID;

    @Schema(description = "구간 경유 정류장/역 목록 (버스/지하철만; 도보는 빈 목록)")
    private List<TransitStopDto> passStops;
}
