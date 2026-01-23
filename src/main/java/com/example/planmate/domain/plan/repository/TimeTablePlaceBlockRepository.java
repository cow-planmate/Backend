package com.example.planmate.domain.plan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TimeTable;
import com.example.planmate.domain.plan.entity.TimeTablePlaceBlock;

public interface TimeTablePlaceBlockRepository extends JpaRepository<TimeTablePlaceBlock, Integer> {
    List<TimeTablePlaceBlock> findByTimeTableTimeTableId(Integer timeTableId);

    List<TimeTablePlaceBlock> findAllByTimeTable(TimeTable timeTable);

    void deleteAllByTimeTable_Plan(Plan timeTablePlan);

    Optional<TimeTablePlaceBlock> findFirstByPlaceIdAndPhotoUrlIsNotNull(String placeId);
}
