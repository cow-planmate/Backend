package com.example.planmate.service;

import com.example.planmate.dto.MakePlanResponse;
import com.example.planmate.entity.*;
import com.example.planmate.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MakePlanService {
    private final PlanRepository planRepository;
    private final TravelRepository travelRepository;
    private final UserRepository userRepository;
    private final TimeTableRepository timeTableRepository;
    private final TransportationCategoryRepository transportationCategoryRepository;
    public MakePlanResponse makeService(int userId, String departure, int travelId,int transportationCategoryId, List<LocalDate> dates, int adultCount, int childCount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다"));

        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 여행지입니다"));

        TransportationCategory transportationCategory = transportationCategoryRepository.findById(transportationCategoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 교통수단입니다"));
        Plan plan = Plan.builder()
                .planName(makePlanName(travel))
                .departure(departure)
                .adultCount(adultCount)
                .childCount(childCount)
                .user(user)
                .travel(travel)
                .transportationCategory(transportationCategory)
                .build();
        Plan savedPlan = planRepository.save(plan);
        for (LocalDate date : dates) {
            TimeTable timeTable = TimeTable.builder()
                    .date(date)
                    .timeTableStartTime(LocalTime.of(9, 0))
                    .timeTableEndTime(LocalTime.of(20, 0))
                    .plan(plan)
                    .build();
            timeTableRepository.save(timeTable);
        }
        MakePlanResponse makePlanResponse = new MakePlanResponse();
        makePlanResponse.setPlanId(savedPlan.getPlanId());
        return makePlanResponse;
    }
    public String makePlanName(Travel travel){
        List<Plan> plans = planRepository.findAll();
        List<Integer> index = new ArrayList<>();
        String travelName = travel.getTravelName();
        for (Plan plan : plans) {
            if(plan.getPlanName().contains(travelName)){
                index.add(Integer.parseInt(plan.getPlanName().substring(travelName.length()+1)));
            }
        }
        Collections.sort(index);

        int i = 1;
        for(Integer index2 : index){
            if(i!=index2){break;}
            i++;
        }
        return travel.getTravelName()+ " " + i;
    }
}
