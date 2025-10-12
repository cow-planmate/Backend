package com.example.planmate.domain.framework.adaptor;

import com.example.planmate.domain.framework.annotation.SocketField;
import org.springframework.stereotype.Service;
import java.lang.reflect.*;

@Service
public class SocketEntityUpdateService {

    /** Request → Entity 반영 */
    public <E, RQ> void applyRequestToEntity(E entity, RQ request) {
        for (Method getter : request.getClass().getDeclaredMethods()) {
            if (!getter.getName().startsWith("get")) continue;
            try {
                Object value = getter.invoke(request);
                if (value == null) continue;

                String fieldName = getter.getName().substring(3);
                Method setter = findSetter(entity.getClass(), fieldName, getter.getReturnType());
                if (setter != null) {
                    Field field = getField(entity.getClass(), fieldName);
                    if (field != null && field.isAnnotationPresent(SocketField.class)
                            && field.getAnnotation(SocketField.class).ignore())
                        continue;
                    setter.invoke(entity, value);
                }
            } catch (Exception ignored) {}
        }
    }

    /** Entity → Response 복사 */
    public <E, RS> void fillResponseFromEntity(E entity, RS response) {
        for (Method getter : entity.getClass().getDeclaredMethods()) {
            if (!getter.getName().startsWith("get")) continue;
            String fieldName = getter.getName().substring(3);
            try {
                Object value = getter.invoke(entity);
                Method setter = findSetter(response.getClass(), fieldName, getter.getReturnType());
                if (setter != null) setter.invoke(response, value);
            } catch (Exception ignored) {}
        }
    }

    private Method findSetter(Class<?> cls, String fieldName, Class<?> paramType) {
        String setterName = "set" + fieldName;
        for (Method m : cls.getDeclaredMethods()) {
            if (m.getName().equalsIgnoreCase(setterName) && m.getParameterCount() == 1)
                return m;
        }
        return null;
    }

    private Field getField(Class<?> cls, String fieldName) {
        try {
            return cls.getDeclaredField(Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1));
        } catch (NoSuchFieldException e) {
            return null;
        }
    }
}