package com.example.planmate.domain.user.dto;

import java.util.ArrayList;
import java.util.List;

import com.example.planmate.common.dto.CommonResponse;
import com.example.planmate.common.valueObject.PreferredThemeVO;
import com.example.planmate.common.valueObject.SimplePlanVO;
import com.example.planmate.domain.user.entity.PreferredTheme;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "마이페이지 정보 조회 응답 데이터")
public class MoveMypageResponse extends CommonResponse {
    @Schema(description = "사용자 고유 식별자", example = "1")
    private int userId;

    @Schema(description = "사용자 이메일", example = "user@example.com")
    private String email;

    @Schema(description = "사용자 닉네임", example = "플랜메이트")
    private String nickname;

    @Schema(description = "사용자 나이대 (예: 20)", example = "20")
    private int age;

    @Schema(description = "사용자 성별 (0: 남성, 1: 여성)", example = "0")
    private int gender;

    @Schema(description = "소셜 로그인 계정 여부", example = "false")
    private boolean isSocialLogin;

    @Schema(description = "직접 생성한 플랜 목록")
    private List<SimplePlanVO> myPlanVOs;

    @Schema(description = "편집 권한이 있는 플랜 목록")
    private List<SimplePlanVO> editablePlanVOs;

    @Setter(AccessLevel.NONE)
    @Schema(description = "사용자 선호 테마 목록")
    private List<PreferredThemeVO> preferredThemes;

    public MoveMypageResponse(){
        preferredThemes= new ArrayList<>();
        myPlanVOs = new ArrayList<>();
        editablePlanVOs = new ArrayList<>();
    }
    public void addPreferredTheme(PreferredTheme preferredTheme) {
        PreferredThemeVO preferredThemeVO = new PreferredThemeVO(
                preferredTheme.getPreferredThemeId(),
                preferredTheme.getPreferredThemeName(),
                preferredTheme.getPreferredThemeCategory().getPreferredThemeCategoryId(),
                preferredTheme.getPreferredThemeCategory().getPreferredThemeCategoryName());
        preferredThemes.add(preferredThemeVO);
    }
    public void addMyPlanVO(int planId, String planName) {
        myPlanVOs.add(new SimplePlanVO(planId, planName));
    }
    public void addEditablePlanVO(int planId, String planName) {
        editablePlanVOs.add(new SimplePlanVO(planId, planName));
    }
}
