package com.example.planmate.domain.framework.service;


import com.example.planmate.domain.framework.adaptor.SocketEntityUpdateService;
import com.example.planmate.domain.framework.annotation.SocketRoot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SocketRootEntityService {

    private final RedisService redisService;  // 프레임워크 내부 or 외부 주입
    private final SocketEntityUpdateService updateService;

    public <E, RQ, RS> RS updateRootEntity(Class<E> rootClass, int id, RQ request, RS response) {
        if (!rootClass.isAnnotationPresent(SocketRoot.class))
            throw new IllegalArgumentException(rootClass.getSimpleName() + " is not a @SocketRoot entity");

        E entity = redisService.findById(rootClass, id);
        updateService.applyRequestToEntity(entity, request);
        redisService.updateEntity(rootClass, entity);
        updateService.fillResponseFromEntity(entity, response);
        return response;
    }

    // TODO: create/delete/read 등 다른 CRUD도 같은 구조로 확장 가능
}
