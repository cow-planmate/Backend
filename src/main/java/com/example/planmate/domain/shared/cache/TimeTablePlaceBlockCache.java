package com.example.planmate.domain.shared.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.example.planmate.domain.image.entity.PlacePhoto;
import com.example.planmate.domain.image.repository.PlacePhotoRepository;
import com.example.planmate.domain.plan.entity.PlaceCategory;
import com.example.planmate.domain.plan.entity.TimeTable;
import com.example.planmate.domain.plan.entity.TimeTablePlaceBlock;
import com.example.planmate.domain.plan.repository.PlaceCategoryRepository;
import com.example.planmate.domain.plan.repository.TimeTablePlaceBlockRepository;
import com.example.planmate.domain.plan.repository.TimeTableRepository;
import com.example.planmate.domain.shared.enums.ECasheKey;
import com.example.planmate.domain.shared.lazydto.TimeTablePlaceBlockDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TimeTablePlaceBlockCache {

    private final TimeTablePlaceBlockRepository timeTablePlaceBlockRepository;
    private final RedisTemplate<String, TimeTablePlaceBlockDto> timeTablePlaceBlockRedis;
    private final AtomicInteger timeTablePlaceBlockTempIdGenerator = new AtomicInteger(-1);
    private final RedisTemplate<String, Integer> timeTableToTimeTablePlaceBlockRedis;
    private final TimeTableRepository timeTableRepository;
    private final PlaceCategoryRepository placeCategoryRepository;
    private final PlacePhotoRepository placePhotoRepository;

    public void deleteRedisTimeTableBlockByTimeTableId(List<Integer> timetableIds) {
        List<Integer> placeBlockIds = timetableIds.stream()
            .map(id -> timeTableToTimeTablePlaceBlockRedis.opsForSet()
                .members(ECasheKey.TIMETABLETOTIMETABLEPLACEBLOCK.key(id)))
            .filter(Objects::nonNull)
            .flatMap(Set::stream)
            .toList();

        List<String> placeBlockKeys = placeBlockIds.stream()
            .map(id -> ECasheKey.TIMETABLEPLACEBLOCK.key(id))
                    .toList();
            timeTablePlaceBlockRedis.delete(placeBlockKeys);

        // 관계(Set) 키들도 함께 제거
        List<String> relationKeys = timetableIds.stream()
            .map(id -> ECasheKey.TIMETABLETOTIMETABLEPLACEBLOCK.key(id))
            .toList();
        timeTableToTimeTablePlaceBlockRedis.delete(relationKeys);
    }

    public List<TimeTablePlaceBlock> findTimeTablePlaceBlocksByTimeTableId(int timetableId) {
        Set<Integer> timeTablePlaceBlocks = timeTableToTimeTablePlaceBlockRedis.opsForSet()
        .members(ECasheKey.TIMETABLETOTIMETABLEPLACEBLOCK.key(timetableId));
        if(timeTablePlaceBlocks == null || timeTablePlaceBlocks.isEmpty()) return Collections.emptyList();

        List<String> keys = new ArrayList<>(timeTablePlaceBlocks.size());
        for(Integer timeTablePlaceBlockId : timeTablePlaceBlocks){
            keys.add(ECasheKey.TIMETABLEPLACEBLOCK.key(timeTablePlaceBlockId));
        }
        List<TimeTablePlaceBlockDto> dtos = timeTablePlaceBlockRedis.opsForValue().multiGet(keys);
        if (dtos == null) return Collections.emptyList();
        TimeTable timeTableRef = timeTableRepository.getReferenceById(timetableId);
        List<TimeTablePlaceBlock> result = new ArrayList<>(dtos.size());
        for (TimeTablePlaceBlockDto dto : dtos) {
            if (dto != null) {
                PlaceCategory pcRef = placeCategoryRepository.getReferenceById(dto.placeCategoryId());
                PlacePhoto ppRef = dto.placeId() != null ? placePhotoRepository.getReferenceById(dto.placeId()) : null;
                result.add(dto.toEntity(pcRef, timeTableRef, ppRef));
            }
        }
        return result;
    }

    public List<TimeTablePlaceBlock> deleteTimeTablePlaceBlockByTimeTableId(int timetableId) {
        String key = ECasheKey.TIMETABLETOTIMETABLEPLACEBLOCK.key(timetableId);
        Set<Integer> timeTablePlaceBlocks = timeTableToTimeTablePlaceBlockRedis.opsForSet().members(key);
        if(timeTablePlaceBlocks == null || timeTablePlaceBlocks.isEmpty()) return Collections.emptyList();

        List<String> keys = new ArrayList<>(timeTablePlaceBlocks.size());
        for(Integer timeTablePlaceBlockId : timeTablePlaceBlocks){
            keys.add(ECasheKey.TIMETABLEPLACEBLOCK.key(timeTablePlaceBlockId));
        }
        List<TimeTablePlaceBlockDto> dtos = timeTablePlaceBlockRedis.opsForValue().multiGet(keys);

        // 관계(Set) 키 제거
        timeTableToTimeTablePlaceBlockRedis.delete(key);

        if (dtos == null) return Collections.emptyList();
        TimeTable timeTableRef = timeTableRepository.getReferenceById(timetableId);
        List<TimeTablePlaceBlock> result = new ArrayList<>(dtos.size());
        for (TimeTablePlaceBlockDto dto : dtos) {
            if (dto != null) {
                PlaceCategory pcRef = placeCategoryRepository.getReferenceById(dto.placeCategoryId());
                PlacePhoto ppRef = dto.placeId() != null ? placePhotoRepository.getReferenceById(dto.placeId()) : null;
                result.add(dto.toEntity(pcRef, timeTableRef, ppRef));
            }
        }
        return result;
    }

    public TimeTablePlaceBlock findTimeTablePlaceBlockByBlockId(int blockId) {
        TimeTablePlaceBlockDto cached = timeTablePlaceBlockRedis.opsForValue().get(ECasheKey.TIMETABLEPLACEBLOCK.key(blockId));
        if (cached == null) {
            throw new IllegalStateException("캐시 누락: Redis에 저장된 TimeTablePlaceBlock 정보가 없습니다: " + blockId);
        }
        PlaceCategory pcRef = placeCategoryRepository.getReferenceById(cached.placeCategoryId());
        TimeTable ttRef = timeTableRepository.getReferenceById(cached.timeTableId());
        PlacePhoto ppRef = cached.placeId() != null ? placePhotoRepository.getReferenceById(cached.placeId()) : null;
        return cached.toEntity(pcRef, ttRef, ppRef);
    }


    public List<TimeTablePlaceBlockDto> insertTimeTablePlaceBlock(int timeTableId) {
        List<TimeTablePlaceBlock> timeTablePlaceBlocks = timeTablePlaceBlockRepository.findByTimeTableTimeTableId(timeTableId);
        List<TimeTablePlaceBlockDto> result = new ArrayList<>();
        for(TimeTablePlaceBlock timeTablePlaceBlock : timeTablePlaceBlocks){
            TimeTablePlaceBlockDto dto = TimeTablePlaceBlockDto.fromEntity(timeTablePlaceBlock);
            timeTablePlaceBlockRedis.opsForValue().set(ECasheKey.TIMETABLEPLACEBLOCK.key(dto.blockId()), dto);
            timeTableToTimeTablePlaceBlockRedis.opsForSet()
                    .add(ECasheKey.TIMETABLETOTIMETABLEPLACEBLOCK.key(timeTableId), dto.blockId());
            result.add(dto);
        }
        return result;
    }


    public TimeTablePlaceBlockDto createTimeTablePlaceBlock(TimeTablePlaceBlockDto timeTablePlaceBlockDto) {
        int tempId = timeTablePlaceBlockTempIdGenerator.getAndDecrement();
        TimeTablePlaceBlockDto updatedDto = timeTablePlaceBlockDto.withBlockId(tempId);
        timeTablePlaceBlockRedis.opsForValue().set(ECasheKey.TIMETABLEPLACEBLOCK.key(tempId), updatedDto);
        timeTableToTimeTablePlaceBlockRedis.opsForSet().add(ECasheKey.TIMETABLETOTIMETABLEPLACEBLOCK.key(updatedDto.timeTableId()), tempId);
        return updatedDto;
    }
    public TimeTablePlaceBlockDto updateTimeTablePlaceBlock(TimeTablePlaceBlockDto timeTablePlaceBlockDto) {
        timeTablePlaceBlockRedis.opsForValue().set(ECasheKey.TIMETABLEPLACEBLOCK.key(timeTablePlaceBlockDto.blockId()), timeTablePlaceBlockDto);
        return timeTablePlaceBlockDto;
    }
    public TimeTablePlaceBlockDto deleteTimeTablePlaceBlock(TimeTablePlaceBlockDto timeTablePlaceBlockDto) {
        timeTablePlaceBlockRedis.delete(ECasheKey.TIMETABLEPLACEBLOCK.key(timeTablePlaceBlockDto.blockId()));
        timeTableToTimeTablePlaceBlockRedis.opsForSet()
        .remove(ECasheKey.TIMETABLETOTIMETABLEPLACEBLOCK.key(timeTablePlaceBlockDto.timeTableId()), timeTablePlaceBlockDto.blockId());
        return timeTablePlaceBlockDto;
    }

    


    public void deleteTimeTablePlaceBlockById(int timeTablePlaceBlockId) {
        timeTablePlaceBlockRedis.delete(ECasheKey.TIMETABLEPLACEBLOCK.key(timeTablePlaceBlockId));
    }
}
