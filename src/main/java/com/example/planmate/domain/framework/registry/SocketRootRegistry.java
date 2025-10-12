package com.example.planmate.domain.framework.registry;

import com.example.planmate.domain.framework.annotation.SocketRoot;
import jakarta.annotation.PostConstruct;
import org.reflections.Reflections;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class SocketRootRegistry {

    private static final Map<String, Class<?>> ROOT_ENTITIES = new HashMap<>();

    @PostConstruct
    public void init() {
        Reflections reflections = new Reflections("com.example"); // 앱 전체 스캔
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(SocketRoot.class);
        for (Class<?> cls : annotated) {
            SocketRoot meta = cls.getAnnotation(SocketRoot.class);
            ROOT_ENTITIES.put(meta.topic(), cls);
        }
    }

    public static Class<?> getEntityClass(String topic) {
        return ROOT_ENTITIES.get(topic);
    }

    public static Set<String> getTopics() {
        return ROOT_ENTITIES.keySet();
    }
}