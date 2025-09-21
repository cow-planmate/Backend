package com.example.planmate.domain.redis.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.planmate.domain.plan.entity.PlaceCategory;
import com.example.planmate.domain.plan.entity.TimeTable;
import com.example.planmate.domain.plan.entity.TimeTablePlaceBlock;
import com.example.planmate.domain.plan.repository.PlaceCategoryRepository;
import com.example.planmate.domain.plan.repository.TimeTablePlaceBlockRepository;
import com.example.planmate.domain.plan.repository.TimeTableRepository;
import com.example.planmate.domain.webSocket.lazydto.TimeTablePlaceBlockDto;
import com.example.planmate.domain.image.repository.PlacePhotoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TimeTablePlaceBlockCacheService {
    private final RedisTemplate<String, TimeTablePlaceBlockDto> timeTablePlaceBlockRedis;
    private final RedisTemplate<String, List<Integer>> timeTableToTimeTablePlaceBlockRedis;
    private static final String TIMETABLEPLACEBLOCK_PREFIX = "TIMETABLEPLACEBLOCK";
    private static final String TIMETABLETOTIMETABLEPLACEBLOCK_PREFIX = "TIMETABLETOTIMETABLEPLACEBLOCK";

    private final TimeTablePlaceBlockRepository timeTablePlaceBlockRepository;
    private final TimeTableRepository timeTableRepository;
    private final PlaceCategoryRepository placeCategoryRepository;
    private final PlacePhotoRepository placePhotoRepository;

    public List<TimeTablePlaceBlock> getByTimeTableId(int timetableId) {
        List<Integer> ids = timeTableToTimeTablePlaceBlockRedis.opsForValue().get(TIMETABLETOTIMETABLEPLACEBLOCK_PREFIX + timetableId);
        if (ids == null) return Collections.emptyList();
        List<String> keys = new ArrayList<>(ids.size());
        for (Integer id : ids) keys.add(TIMETABLEPLACEBLOCK_PREFIX + id);
        List<TimeTablePlaceBlockDto> dtos = timeTablePlaceBlockRedis.opsForValue().multiGet(keys);
        if (dtos == null) return Collections.emptyList();
        TimeTable timeTableRef = timeTableRepository.getReferenceById(timetableId);
        List<TimeTablePlaceBlock> result = new ArrayList<>(dtos.size());
        for (TimeTablePlaceBlockDto dto : dtos) {
            if (dto != null) {
                PlaceCategory pcRef = placeCategoryRepository.getReferenceById(dto.placeCategoryId());
        // DTO carries placeId. Use reference to avoid hitting DB unless accessed later.
        result.add(dto.toEntity(pcRef, timeTableRef,
            dto.placeId() != null ? placePhotoRepository.getReferenceById(dto.placeId()) : null));
            }
        }
        return result;
    }

    public List<TimeTablePlaceBlock> deleteByTimeTableId(int timetableId) {
        List<Integer> ids = timeTableToTimeTablePlaceBlockRedis.opsForValue().getAndDelete(TIMETABLETOTIMETABLEPLACEBLOCK_PREFIX + timetableId);
        if (ids == null) return Collections.emptyList();
        List<String> keys = new ArrayList<>(ids.size());
        for (Integer id : ids) keys.add(TIMETABLEPLACEBLOCK_PREFIX + id);
        List<TimeTablePlaceBlockDto> dtos = timeTablePlaceBlockRedis.opsForValue().multiGet(keys);
        if (dtos == null) return Collections.emptyList();
        TimeTable timeTableRef = timeTableRepository.getReferenceById(timetableId);
        List<TimeTablePlaceBlock> result = new ArrayList<>(dtos.size());
    for (TimeTablePlaceBlockDto dto : dtos) {
            if (dto != null) {
                PlaceCategory pcRef = placeCategoryRepository.getReferenceById(dto.placeCategoryId());
        result.add(dto.toEntity(pcRef, timeTableRef,
            dto.placeId() != null ? placePhotoRepository.getReferenceById(dto.placeId()) : null));
            }
        }
        return result;
    }

    public void deleteByTimeTableIds(List<Integer> timetableIds) {
        if (timetableIds == null || timetableIds.isEmpty()) return;
        // gather all place block ids
        List<Integer> allPlaceBlockIds = new ArrayList<>();
        for (Integer ttId : timetableIds) {
            List<Integer> ids = timeTableToTimeTablePlaceBlockRedis.opsForValue().get(TIMETABLETOTIMETABLEPLACEBLOCK_PREFIX + ttId);
            if (ids != null) allPlaceBlockIds.addAll(ids);
        }
        if (allPlaceBlockIds.isEmpty()) return;
        List<String> placeBlockKeys = new ArrayList<>(allPlaceBlockIds.size());
        for (Integer id : allPlaceBlockIds) placeBlockKeys.add(TIMETABLEPLACEBLOCK_PREFIX + id);
        timeTablePlaceBlockRedis.delete(placeBlockKeys);
    }

    public TimeTablePlaceBlock get(int blockId) {
        TimeTablePlaceBlockDto cached = timeTablePlaceBlockRedis.opsForValue().get(TIMETABLEPLACEBLOCK_PREFIX + blockId);
        if (cached == null) throw new IllegalStateException("캐시 누락: Redis에 저장된 TimeTablePlaceBlock 정보가 없습니다.");
        PlaceCategory pcRef = placeCategoryRepository.getReferenceById(cached.placeCategoryId());
        TimeTable ttRef = timeTableRepository.getReferenceById(cached.timeTableId());
    return cached.toEntity(pcRef, ttRef,
        cached.placeId() != null ? placePhotoRepository.getReferenceById(cached.placeId()) : null);
    }

    public List<TimeTablePlaceBlockDto> register(int timeTableId) {
        List<TimeTablePlaceBlock> list = timeTablePlaceBlockRepository.findByTimeTableTimeTableId(timeTableId);
        List<Integer> ids = new ArrayList<>();
        List<TimeTablePlaceBlockDto> result = new ArrayList<>();
        for (TimeTablePlaceBlock entity : list) {
            TimeTablePlaceBlockDto dto = TimeTablePlaceBlockDto.fromEntity(entity);
            timeTablePlaceBlockRedis.opsForValue().set(TIMETABLEPLACEBLOCK_PREFIX + dto.blockId(), dto);
            ids.add(dto.blockId());
            result.add(dto);
        }
        timeTableToTimeTablePlaceBlockRedis.opsForValue().set(TIMETABLETOTIMETABLEPLACEBLOCK_PREFIX + timeTableId, ids);
        return result;
    }

    public void addBlockIdToTimeTableIndex(int timeTableId, int blockId) {
        List<Integer> ids = timeTableToTimeTablePlaceBlockRedis.opsForValue().get(TIMETABLETOTIMETABLEPLACEBLOCK_PREFIX + timeTableId);
        if (ids == null) ids = new ArrayList<>();
        ids.add(blockId);
        timeTableToTimeTablePlaceBlockRedis.opsForValue().set(TIMETABLETOTIMETABLEPLACEBLOCK_PREFIX + timeTableId, ids);
    }

    public void removeBlockIdFromTimeTableIndex(int timeTableId, int blockId) {
        List<Integer> ids = timeTableToTimeTablePlaceBlockRedis.opsForValue().get(TIMETABLETOTIMETABLEPLACEBLOCK_PREFIX + timeTableId);
        if (ids != null) {
            ids.remove(Integer.valueOf(blockId));
            timeTableToTimeTablePlaceBlockRedis.opsForValue().set(TIMETABLETOTIMETABLEPLACEBLOCK_PREFIX + timeTableId, ids);
        }
    }

    public int registerNew(int timeTableId, TimeTablePlaceBlock block, int tempId) {
        block.changeId(tempId);
        timeTablePlaceBlockRedis.opsForValue().set(TIMETABLEPLACEBLOCK_PREFIX + block.getBlockId(), TimeTablePlaceBlockDto.fromEntity(block));
        addBlockIdToTimeTableIndex(timeTableId, block.getBlockId());
        return tempId;
    }

    public void delete(int timeTableId, int blockId) {
        timeTablePlaceBlockRedis.delete(TIMETABLEPLACEBLOCK_PREFIX + blockId);
        removeBlockIdFromTimeTableIndex(timeTableId, blockId);
    }

    public void update(TimeTablePlaceBlock block) {
        timeTablePlaceBlockRedis.opsForValue().set(TIMETABLEPLACEBLOCK_PREFIX + block.getBlockId(), TimeTablePlaceBlockDto.fromEntity(block));
    }
}
