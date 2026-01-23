package com.example.planmate.domain.plan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TimeTable;
import com.example.planmate.domain.plan.entity.TimeTablePlaceBlock;

public interface TimeTablePlaceBlockRepository extends JpaRepository<TimeTablePlaceBlock, Integer> {
    List<TimeTablePlaceBlock> findByTimeTableTimeTableId(Integer timeTableId);

    List<TimeTablePlaceBlock> findAllByTimeTable(TimeTable timeTable);

    void deleteAllByTimeTable_Plan(Plan timeTablePlan);

    Optional<TimeTablePlaceBlock> findFirstByPlaceIdAndPhotoUrlIsNotNull(String placeId);

    @Modifying
    @Transactional
    @Query("UPDATE TimeTablePlaceBlock b SET b.photoUrl = :photoUrl WHERE b.placeId = :placeId AND (b.photoUrl IS NULL OR b.photoUrl = '')")
    void updatePhotoUrlByPlaceId(@Param("placeId") String placeId, @Param("photoUrl") String photoUrl);
}
