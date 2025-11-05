package com.sharedsync.framework.shared.sync;

import java.util.List;

import com.example.planmate.generated.cache.PlanCache;
import com.example.planmate.generated.cache.TimeTableCache;
import com.example.planmate.generated.cache.TimeTablePlaceBlockCache;
import com.example.planmate.generated.lazydto.PlanDto;
import com.example.planmate.generated.lazydto.TimeTableDto;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CacheSyncService {

    private final PlanCache planCache;
    private final TimeTableCache timeTableCache;
    private final TimeTablePlaceBlockCache timeTablePlaceBlockCache;


    public void syncToDatabase(int planId) {
        PlanDto planDto = planCache.findDtoById(planId);
        if (planDto != null) {
            planCache.syncToDatabase(planDto);
        }

        // 타임테이블 동기화 및 삭제 반영
        timeTableCache.syncCollectionByParentId(planId);

        // 최신 타임테이블 목록 기준으로 장소 블록 동기화 및 삭제 반영
        List<TimeTableDto> refreshedTimeTables = timeTableCache.findDtoListByParentID(planId);
        for (TimeTableDto timeTableDto : refreshedTimeTables) {
            Integer timeTableId = timeTableDto.timeTableId();
            if (timeTableId != null && timeTableId > 0) {
                timeTablePlaceBlockCache.syncCollectionByParentId(timeTableId);
            }
        }
    }

    // public void syncToDatabase(int planId) {
    //     // Cache에서 Plan 데이터 가져오기
    //     Optional<Plan> cachedPlanOpt = planCache.findById(planId);
    //     planCache.deleteById(planId);
    //     if (cachedPlanOpt.isEmpty()) {
    //         return; // 캐시에 데이터가 없으면 동기화할 필요 없음                                                    
    //     }
        
    //     Plan existingPlan = planRepository.findById(planId)
    //         .orElseThrow(() -> new PlanNotFoundException());
    //     planCache.mergeEntityFields(existingPlan, cachedPlanOpt.get());
    //     Plan savedPlan = planRepository.save(existingPlan);

    //     // TimeTable 동기화
    //     List<TimeTable> oldTimetables = timeTableRepository.findByPlanPlanId(planId);
    //     Map<Integer, TimeTable> tempIdToEntity = new HashMap<>();
        
    //     // Cache에서 TimeTable 가져오기 (DB가 아닌 캐시에서)
    //     List<TimeTable> cachedTimeTables = timeTableCache.findByParentId(planId);
    //     timeTableCache.deleteByParentId(planId);
        
    //     for (TimeTable cached : cachedTimeTables) {
    //         int tempId = cached.getTimeTableId();
    //         TimeTable timeTable;
            
    //         if (tempId < 0) {
    //             // 새로운 테이블 (음수 ID)
    //             timeTable = TimeTable.builder()
    //                     .timeTableId(null)
    //                     .date(cached.getDate())
    //                     .timeTableStartTime(cached.getTimeTableStartTime())
    //                     .timeTableEndTime(cached.getTimeTableEndTime())
    //                     .plan(savedPlan)
    //                     .build();
    //         } else {
    //             // 기존 테이블 (양수 ID) - mergeEntityFields로 자동 병합
    //             TimeTable existing = timeTableRepository.findById(tempId).orElse(null);
    //             if (existing == null) continue;
                
    //             timeTableCache.mergeEntityFields(existing, cached);
    //             oldTimetables.removeIf(ot -> ot.getTimeTableId() != null && ot.getTimeTableId().equals(tempId));
    //             timeTable = existing;
    //         }
    //         timeTableRepository.save(timeTable);
    //         tempIdToEntity.put(tempId, timeTable);
    //     }
    //     timeTableRepository.deleteAll(oldTimetables);

    //     // TimeTablePlaceBlock 동기화
    //     List<TimeTablePlaceBlock> newBlocks = new ArrayList<>();
    //     List<TimeTablePlaceBlock> deletedBlocks = new ArrayList<>();
        
    //     for (Map.Entry<Integer, TimeTable> entry : tempIdToEntity.entrySet()) {
    //         int tempTimeTableId = entry.getKey();
    //         TimeTable realTimeTable = entry.getValue();
            
    //         // Cache에서 블록들 가져오기
    //         List<TimeTablePlaceBlock> cachedBlocks = timeTablePlaceBlockCache.findByParentId(tempTimeTableId);
            
    //         if (tempTimeTableId < 0) {
    //             // 새로 생성된 TimeTable의 블록들
    //             cachedBlocks.forEach(block -> {
    //                 block.assignTimeTable(realTimeTable);
    //                 block.changeId(null);
    //                 newBlocks.add(block);
    //             });
    //         } else {
    //             // 기존 TimeTable의 블록들
    //             List<Integer> updatedBlockIds = new ArrayList<>();
                
    //             for (TimeTablePlaceBlock cached : cachedBlocks) {
    //                 if (cached.getBlockId() >= 0) {
    //                     // 기존 블록 업데이트 - mergeEntityFields로 자동 병합
    //                     TimeTablePlaceBlock existing = timeTablePlaceBlockRepository.findById(cached.getBlockId())
    //                         .orElseThrow(() -> new IllegalArgumentException("블록을 찾을 수 없습니다. ID=" + cached.getBlockId()));
    //                     timeTablePlaceBlockCache.mergeEntityFields(existing, cached);
    //                     updatedBlockIds.add(cached.getBlockId());
    //                 } else {
    //                     // 새 블록 추가
    //                     cached.assignTimeTable(realTimeTable);
    //                     cached.changeId(null);
    //                     newBlocks.add(cached);
    //                 }
    //             }
                
    //             // 삭제된 블록 수집
    //             List<TimeTablePlaceBlock> oldBlocks = timeTablePlaceBlockRepository.findByTimeTableTimeTableId(tempTimeTableId);
    //             oldBlocks.removeIf(old -> updatedBlockIds.contains(old.getBlockId()));
    //             deletedBlocks.addAll(oldBlocks);
    //         }
    //     }

    //     timeTablePlaceBlockRepository.deleteAll(deletedBlocks);
    //     timeTablePlaceBlockRepository.saveAll(newBlocks);
    //     tempIdToEntity.keySet().forEach(timeTablePlaceBlockCache::deleteByParentId);
    // }
}