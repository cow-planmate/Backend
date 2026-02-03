package com.example.planmate.domain.plan.dto;

import java.util.List;

import com.example.planmate.common.dto.CommonResponse;
import com.example.planmate.common.valueObject.SimplePlanVO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "나의 플랜 목록 조회 응답 데이터")
public class GetMyPlansResponse extends CommonResponse {
    @Schema(description = "직접 생성한 플랜 목록")
    private List<SimplePlanVO> myPlans;

    @Schema(description = "편집 권한이 있는 플랜 목록")
    private List<SimplePlanVO> editablePlans;
}
