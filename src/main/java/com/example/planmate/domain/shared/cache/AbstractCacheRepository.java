package com.example.planmate.domain.shared.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;

/**
 * JPA Repository처럼 자동으로 기본 구현을 제공하는 추상 캐시 클래스
 * 이 클래스를 상속받으면 기본 CRUD 메서드들이 자동으로 구현됩니다.
 * 
 * @param <T> 엔티티 타입
 * @param <ID> ID 타입  
 * @param <DTO> DTO 타입
 */
public abstract class AbstractCacheRepository<T, ID, DTO> implements CacheRepository<T, ID, DTO> {

    // ==== 하위 클래스에서 구현해야 할 추상 메서드들 ====
    
    /**
     * Redis 키 생성 (예: "plan:1", "timetable:2")
     */
    protected abstract String getRedisKey(ID id);
    
    /**
     * Redis Template 반환
     */
    protected abstract RedisTemplate<String, DTO> getRedisTemplate();
    
    /**
     * DTO를 엔티티로 변환
     */
    protected abstract T convertToEntity(DTO dto);
    
    // ==== 자동 구현되는 기본 CRUD 메서드들 ====
    
    @Override
    public Optional<T> findById(ID id) {
        DTO dto = getRedisTemplate().opsForValue().get(getRedisKey(id));
        if (dto == null) {
            return Optional.empty();
        }
        return Optional.of(convertToEntity(dto));
    }
    
    @Override
    public T getReferenceById(ID id) {
        return findById(id).orElseThrow(() -> 
            new IllegalStateException("캐시에서 데이터를 찾을 수 없습니다: " + id));
    }
    
    @Override
    public DTO save(DTO dto) {
        ID id = extractId(dto);
        getRedisTemplate().opsForValue().set(getRedisKey(id), dto);
        return dto;
    }
    
    @Override
    public void deleteById(ID id) {
        getRedisTemplate().delete(getRedisKey(id));
    }
    
    @Override
    public boolean existsById(ID id) {
        return Boolean.TRUE.equals(getRedisTemplate().hasKey(getRedisKey(id)));
    }
    
    @Override
    public List<T> findAllById(Iterable<ID> ids) {
        List<String> keys = new ArrayList<>();
        ids.forEach(id -> keys.add(getRedisKey(id)));
        
        List<DTO> dtos = getRedisTemplate().opsForValue().multiGet(keys);
        if (dtos == null) {
            return Collections.emptyList();
        }
        
        return dtos.stream()
            .filter(dto -> dto != null)
            .map(this::convertToEntity)
            .toList();
    }
    
    @Override
    public List<DTO> saveAll(List<DTO> dtos) {
        dtos.forEach(dto -> {
            ID id = extractId(dto);
            getRedisTemplate().opsForValue().set(getRedisKey(id), dto);
        });
        return dtos;
    }
    
    @Override
    public void deleteAllById(Iterable<ID> ids) {
        List<String> keys = new ArrayList<>();
        ids.forEach(id -> keys.add(getRedisKey(id)));
        getRedisTemplate().delete(keys);
    }
    
    // ==== 유틸리티 메서드 (하위 클래스에서 필요시 오버라이드) ====
    
    /**
     * DTO에서 ID 추출 (하위 클래스에서 구현)
     */
    protected abstract ID extractId(DTO dto);
    
    /**
     * 캐시 전체 초기화 (선택적 구현)
     */
    @Override
    public void clear() {
        // 기본적으로는 지원하지 않음 - 필요한 캐시에서만 오버라이드
        throw new UnsupportedOperationException("clear operation not supported");
    }
}