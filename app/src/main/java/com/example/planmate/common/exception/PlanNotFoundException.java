package com.example.planmate.common.exception;

public class PlanNotFoundException extends RuntimeException {
    public PlanNotFoundException() {
        super("존재하지 않는 플랜 입니다.");
    }
}
