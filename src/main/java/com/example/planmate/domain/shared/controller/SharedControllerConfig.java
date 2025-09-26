package com.example.planmate.domain.shared.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.planmate.domain.shared.dto.WPlanRequest;
import com.example.planmate.domain.shared.dto.WPlanResponse;
import com.example.planmate.domain.shared.dto.WTimeTablePlaceBlockRequest;
import com.example.planmate.domain.shared.dto.WTimeTablePlaceBlockResponse;
import com.example.planmate.domain.shared.dto.WTimetableRequest;
import com.example.planmate.domain.shared.dto.WTimetableResponse;
import com.example.planmate.domain.shared.service.SharedPlanService;
import com.example.planmate.domain.shared.service.SharedTimeTablePlaceBlockService;
import com.example.planmate.domain.shared.service.SharedTimeTableService;

@Configuration
public class SharedControllerConfig {

    @Bean
    public SharedContoller<WPlanRequest, WPlanResponse, SharedPlanService> planController(SharedPlanService service) {
        return new SharedContoller<WPlanRequest, WPlanResponse, SharedPlanService>(service) {
            @Override
            protected String getEntityName() {
                return "plan";
            }
            @Override
            protected String getRootEntityName() {
                return "plan";
            }
        };
    }

    @Bean
    public SharedContoller<WTimetableRequest, WTimetableResponse, SharedTimeTableService> timetableController(SharedTimeTableService service) {
        return new SharedContoller<WTimetableRequest, WTimetableResponse, SharedTimeTableService>(service) {
            @Override
            protected String getEntityName() {
                return "timetable";
            }
            @Override
            protected String getRootEntityName() {
                return "plan";
            }
        };
    }

    @Bean
    public SharedContoller<WTimeTablePlaceBlockRequest, WTimeTablePlaceBlockResponse, SharedTimeTablePlaceBlockService> timetablePlaceBlockController(SharedTimeTablePlaceBlockService service) {
        return new SharedContoller<WTimeTablePlaceBlockRequest, WTimeTablePlaceBlockResponse, SharedTimeTablePlaceBlockService>(service) {
            @Override
            protected String getEntityName() {
                return "timetableplaceblock";
            }
            @Override
            protected String getRootEntityName() {
                return "plan";
            }
        };
    }
}