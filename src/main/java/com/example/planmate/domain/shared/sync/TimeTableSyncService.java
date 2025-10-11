package com.example.planmate.domain.shared.sync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TimeTable;
import com.example.planmate.domain.plan.repository.TimeTableRepository;
import com.example.planmate.domain.shared.cache.TimeTableCache;

import jakarta.persistence.EntityManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TimeTableSyncService {

    private final TimeTableRepository timeTableRepository;
    private final TimeTableCache timeTableCache;
    private final EntityManager entityManager;

    public TimeTableSyncResult syncTimeTables(Plan savedPlan) {
        // Delegate to id-based overload to reduce coupling to JPA entity outside this service
        return syncTimeTables(savedPlan.getPlanId());
    }

    public TimeTableSyncResult syncTimeTables(int planId) {
        // Use a lightweight reference to avoid loading the full Plan entity
        Plan planRef = entityManager.getReference(Plan.class, planId);
        List<TimeTable> cachedTimetables = timeTableCache.deleteByParentId(planId);

        List<TimeTable> newTimetables = new ArrayList<>();
        List<TimeTable> oldTimetables = timeTableRepository.findByPlanPlanId(planId);

        Map<Integer, TimeTable> changeTimeTable = new HashMap<>();     // tempId<0 -> persisted entity
        Map<Integer, TimeTable> notChangeTimeTable = new HashMap<>();  // id>=0   -> managed entity

        if (cachedTimetables != null) {
            for (TimeTable t : cachedTimetables) {
                Integer tempId = t.getTimeTableId();
                if (tempId == null) continue;

                if (tempId < 0) {
                    // 신규 타임테이블 생성 (실제 엔티티를 tempId에 매핑)
                    TimeTable newT = TimeTable.builder()
                            .timeTableId(null)
                            .date(t.getDate())
                            .timeTableStartTime(t.getTimeTableStartTime())
                            .timeTableEndTime(t.getTimeTableEndTime())
                            .plan(planRef)
                            .build();
                    newTimetables.add(newT);
                    changeTimeTable.put(tempId, newT);
                } else {
                    // 기존 타임테이블 업데이트
                    TimeTable existing = timeTableRepository.findById(tempId).orElse(null);
                    if (existing != null) {
                        existing.changeDate(t.getDate());
                        existing.changeTime(t.getTimeTableStartTime(), t.getTimeTableEndTime());
                        // 삭제 대상(oldTimetables)에서 제외
                        oldTimetables.removeIf(ot ->
                                ot.getTimeTableId() != null && ot.getTimeTableId().equals(existing.getTimeTableId())
                        );
                        notChangeTimeTable.put(tempId, existing);
                    }
                }
            }
        }

        timeTableRepository.saveAll(newTimetables);      // 신규 영속 -> ID 채워짐
        timeTableRepository.deleteAll(oldTimetables);    // 제거 대상 삭제

        List<Integer> deleteTimeTableIds = new ArrayList<>();
        deleteTimeTableIds.addAll(changeTimeTable.keySet());
        deleteTimeTableIds.addAll(notChangeTimeTable.keySet());

        return new TimeTableSyncResult(changeTimeTable, notChangeTimeTable, deleteTimeTableIds);
    }

    @Getter
    public static class TimeTableSyncResult {
        private final Map<Integer, TimeTable> changeTimeTable;     // tempId<0 -> persisted TimeTable
        private final Map<Integer, TimeTable> notChangeTimeTable;  // id>=0    -> managed TimeTable
        private final List<Integer> deleteTimeTableIds;

        public TimeTableSyncResult(Map<Integer, TimeTable> changeTimeTable,
                                   Map<Integer, TimeTable> notChangeTimeTable,
                                   List<Integer> deleteTimeTableIds) {
            this.changeTimeTable = changeTimeTable;
            this.notChangeTimeTable = notChangeTimeTable;
            this.deleteTimeTableIds = deleteTimeTableIds;
        }
    }
}
