package com.example.planmate.domain.plan.repository;

import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TimeTable;
import com.example.planmate.domain.plan.entity.TimeTablePlaceBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimeTablePlaceBlockRepository extends JpaRepository<TimeTablePlaceBlock, Integer> {
    List<TimeTablePlaceBlock> findByTimeTableTimeTableId(Integer timeTableId);

    List<TimeTablePlaceBlock> findAllByTimeTable(TimeTable timeTable);

    void deleteAllByTimeTable_Plan(Plan timeTablePlan);
}
