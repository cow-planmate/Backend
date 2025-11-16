package com.example.planmate.move.shared.presence.core;

import com.example.planmate.move.shared.presence.annotation.PresenceUser;
import org.reflections.Reflections;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.Set;

@Component
public class PresenceUserRegistry {

    private Class<?> userClass;
    private Field idField;
    private Field nameField;

    @PostConstruct
    public void init() {
        Reflections reflections = new Reflections("com.example"); // TODO: 추후 동적/플러그인화
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(PresenceUser.class);
        if (annotated.isEmpty()) return;

        userClass = annotated.iterator().next();
        PresenceUser ann = userClass.getAnnotation(PresenceUser.class);
        try {
            idField = userClass.getDeclaredField(ann.idField());
            nameField = userClass.getDeclaredField(ann.nameField());
            idField.setAccessible(true);
            nameField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("@PresenceUser fields not found in " + userClass.getName(), e);
        }
    }

    /** 등록된 User 엔티티 클래스 반환 (없으면 null) */
    public Class<?> getUserClass() {
        return userClass;
    }

    /** 스캔 성공 여부 */
    public boolean isInitialized() {
        return userClass != null && idField != null && nameField != null;
    }

    public boolean isUserClass(Object obj) {
        return isInitialized() && userClass.isAssignableFrom(obj.getClass());
    }

    public int getUserId(Object user) {
        try {
            return (int) idField.get(user);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getNickname(Object user) {
        try {
            return (String) nameField.get(user);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
