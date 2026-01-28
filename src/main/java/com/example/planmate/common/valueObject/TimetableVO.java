package com.example.planmate.common.valueObject;

import java.time.LocalDate;
import java.time.LocalTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "타임테이블(일차별) 정보")
public class TimetableVO {
    @Schema(description = "타임테이블 ID", example = "1")
    private Integer timeTableId;

    @Schema(description = "해당 일차 날짜", example = "2023-10-01")
    private LocalDate date;

    @Schema(description = "일차 시작 시간", example = "09:00:00")
    private LocalTime timeTableStartTime;

    @Schema(description = "일차 종료 시간", example = "22:00:00")
    private LocalTime timeTableEndTime;
}
