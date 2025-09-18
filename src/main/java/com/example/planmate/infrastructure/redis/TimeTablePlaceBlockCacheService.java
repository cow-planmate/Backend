package com.example.planmate.infrastructure.redis;

import static com.example.planmate.infrastructure.redis.RedisKeys.timeTableBlock;
import static com.example.planmate.infrastructure.redis.RedisKeys.timeTableToBlocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.planmate.domain.image.entity.PlacePhoto;
import com.example.planmate.domain.image.repository.PlacePhotoRepository;
import com.example.planmate.domain.plan.entity.PlaceCategory;
import com.example.planmate.domain.plan.entity.TimeTable;
import com.example.planmate.domain.plan.entity.TimeTablePlaceBlock;
import com.example.planmate.domain.plan.repository.PlaceCategoryRepository;
import com.example.planmate.domain.plan.repository.TimeTablePlaceBlockRepository;
import com.example.planmate.domain.plan.repository.TimeTableRepository;
import com.example.planmate.domain.webSocket.lazydto.TimeTablePlaceBlockDto;

@Service
public class TimeTablePlaceBlockCacheService {
    private final RedisTemplate<String, TimeTablePlaceBlockDto> blockRedis;
    private final RedisTemplate<String, List<Integer>> timeTableToBlocksRedis;
    private final TimeTablePlaceBlockRepository blockRepository;
    private final TimeTableRepository timeTableRepository;
    private final PlaceCategoryRepository placeCategoryRepository;
    private final PlacePhotoRepository placePhotoRepository;
    private final AtomicInteger tempIdGenerator = new AtomicInteger(-1);

    @Autowired
    public TimeTablePlaceBlockCacheService(
            @Qualifier("timeTablePlaceBlockRedis") RedisTemplate<String, TimeTablePlaceBlockDto> blockRedis,
            @Qualifier("timeTableToTimeTablePlaceBlockRedis") RedisTemplate<String, List<Integer>> timeTableToBlocksRedis,
            TimeTablePlaceBlockRepository blockRepository,
            TimeTableRepository timeTableRepository,
            PlaceCategoryRepository placeCategoryRepository,
            PlacePhotoRepository placePhotoRepository
    ){
        this.blockRedis = blockRedis;
        this.timeTableToBlocksRedis = timeTableToBlocksRedis;
        this.blockRepository = blockRepository;
        this.timeTableRepository = timeTableRepository;
        this.placeCategoryRepository = placeCategoryRepository;
        this.placePhotoRepository = placePhotoRepository;
    }

    public List<TimeTablePlaceBlock> getByTimeTable(int timeTableId){
        List<Integer> ids = timeTableToBlocksRedis.opsForValue().get(timeTableToBlocks(timeTableId));
        if(ids==null) return null;
        List<String> keys = new ArrayList<>();
        for(Integer id : ids){ keys.add(timeTableBlock(id)); }
        List<TimeTablePlaceBlockDto> dtos = blockRedis.opsForValue().multiGet(keys);
        if(dtos==null) return Collections.emptyList();
        TimeTable ttRef = timeTableRepository.getReferenceById(timeTableId);
        List<TimeTablePlaceBlock> result = new ArrayList<>(dtos.size());
        for(TimeTablePlaceBlockDto dto : dtos){
            if(dto!=null){
                PlaceCategory pcRef = placeCategoryRepository.getReferenceById(dto.placeCategoryId());
                PlacePhoto ppRef = dto.placeId()!=null ? placePhotoRepository.getReferenceById(dto.placeId()) : null;
                result.add(dto.toEntity(pcRef, ttRef, ppRef));
            }
        }
        return result;
    }

    public void loadForTimeTable(int timeTableId){
        List<TimeTablePlaceBlock> blocks = blockRepository.findByTimeTableTimeTableId(timeTableId);
        List<Integer> ids = new ArrayList<>();
        for(TimeTablePlaceBlock b : blocks){
            TimeTablePlaceBlockDto dto = TimeTablePlaceBlockDto.fromEntity(b);
            blockRedis.opsForValue().set(timeTableBlock(dto.blockId()), dto);
            ids.add(dto.blockId());
        }
        timeTableToBlocksRedis.opsForValue().set(timeTableToBlocks(timeTableId), ids);
    }

    public int addNew(int timeTableId, TimeTablePlaceBlock block){
        int tempId = tempIdGenerator.getAndDecrement();
        block.changeId(tempId);
        blockRedis.opsForValue().set(timeTableBlock(block.getBlockId()), TimeTablePlaceBlockDto.fromEntity(block));
        List<Integer> ids = timeTableToBlocksRedis.opsForValue().get(timeTableToBlocks(timeTableId));
        if(ids==null) ids = new ArrayList<>();
        ids.add(block.getBlockId());
        timeTableToBlocksRedis.opsForValue().set(timeTableToBlocks(timeTableId), ids);
        return tempId;
    }

    public void update(TimeTablePlaceBlock block){
        blockRedis.opsForValue().set(timeTableBlock(block.getBlockId()), TimeTablePlaceBlockDto.fromEntity(block));
    }
}
