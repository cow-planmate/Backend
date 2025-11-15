package com.sharedsync.framework.shared.framework.dto;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.sharedsync.framework.shared.framework.annotation.CacheDtoField;
import com.sharedsync.framework.shared.framework.annotation.EntityReference;

import jakarta.persistence.Id;

/**
 * Convenience base class for cache DTOs that mirror JPA entities.
 * <p>
 * Simple scalar fields are copied by name. Fields annotated with {@link EntityReference}
 * are automatically mapped via the referenced entity's {@code @Id} field. Entity re-creation
 * uses a Lombok-style {@code builder()} method when available.
 * </p>
 *
 * @param <ID> identifier type
 * @param <E>  mapped entity type
 */
public abstract class EntityBackedCacheDto<ID, E> extends CacheDto<ID> {

    private static final ConcurrentMap<Class<?>, MappingMetadata<?, ?>> METADATA_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Class<?>, Field> ENTITY_ID_FIELD_CACHE = new ConcurrentHashMap<>();

    private final Class<E> entityClass;

    protected EntityBackedCacheDto() {
        super();
        this.entityClass = resolveEntityClass();
    }

    /**
     * Populates the current DTO instance from the supplied entity using reflection.
     */
    protected void populateFromEntity(E entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        metadata().copyEntityToDto(entity, this);
    }

    /**
     * Converts the current DTO into its corresponding entity, resolving referenced entities
     * from the supplied varargs in the declaration order of {@link EntityReference} fields.
     */
    @com.sharedsync.framework.shared.framework.annotation.EntityConverter
    public E toEntity(Object... relatedEntities) {
        Object[] safeArgs = relatedEntities == null ? new Object[0] : relatedEntities;
        return metadata().buildEntityFromDto(this, safeArgs);
    }

    /**
     * Utility for subclasses to create an instance populated from an entity.
     */
    protected static <ID, E, D extends EntityBackedCacheDto<ID, E>> D instantiateFromEntity(E entity, Class<D> dtoClass) {
        try {
            D dto = dtoClass.getDeclaredConstructor().newInstance();
            dto.populateFromEntity(entity);
            return dto;
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to instantiate DTO " + dtoClass.getSimpleName(), ex);
        }
    }

    @SuppressWarnings("unchecked")
    private MappingMetadata<ID, E> metadata() {
        return (MappingMetadata<ID, E>) METADATA_CACHE.computeIfAbsent(getClass(), key -> MappingMetadata.create(key, entityClass));
    }

    @SuppressWarnings("unchecked")
    private Class<E> resolveEntityClass() {
        Class<?> current = getClass();
        while (current != null && current != Object.class) {
            Type genericSuper = current.getGenericSuperclass();
            if (genericSuper instanceof ParameterizedType parameterized) {
                Type raw = parameterized.getRawType();
                if (raw instanceof Class<?> rawClass && EntityBackedCacheDto.class.equals(rawClass)) {
                    Type entityType = parameterized.getActualTypeArguments()[1];
                    if (entityType instanceof Class<?>) {
                        return (Class<E>) entityType;
                    }
                }
            }
            current = current.getSuperclass();
        }
        throw new IllegalStateException("Unable to resolve entity type for " + getClass().getSimpleName());
    }

    private static Field resolveEntityIdField(Class<?> entityType) {
        return ENTITY_ID_FIELD_CACHE.computeIfAbsent(entityType, cls -> {
            Class<?> current = cls;
            while (current != null && current != Object.class) {
                for (Field field : current.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Id.class)) {
                        field.setAccessible(true);
                        return field;
                    }
                }
                current = current.getSuperclass();
            }
            throw new IllegalStateException("No @Id field found on " + cls.getName());
        });
    }

    private record FieldMapping(Field dtoField, Field entityField, Method builderSetter) {}

    private record ReferenceMapping(Field dtoField, Field entityField, Method builderSetter, EntityReference reference) {}

    private record DtoFieldMapping(Field dtoField, Field entityField, Method factoryMethod) {}

    private static final class MappingMetadata<ID, E> {
        private final Class<E> entityType;
        private final Method builderFactory;
        private final Method buildMethod;
        private final List<FieldMapping> scalarMappings;
        private final List<ReferenceMapping> referenceMappings;
        private final List<DtoFieldMapping> dtoFieldMappings;

        private MappingMetadata(Class<E> entityType, Method builderFactory, Method buildMethod,
                                List<FieldMapping> scalarMappings, List<ReferenceMapping> referenceMappings,
                                List<DtoFieldMapping> dtoFieldMappings) {
            this.entityType = entityType;
            this.builderFactory = builderFactory;
            this.buildMethod = buildMethod;
            this.scalarMappings = scalarMappings;
            this.referenceMappings = referenceMappings;
            this.dtoFieldMappings = dtoFieldMappings;
        }

        static <ID, E> MappingMetadata<ID, E> create(Class<?> dtoType, Class<E> entityType) {
            try {
                Method builderFactory = entityType.getMethod("builder");
                Object builderInstance = builderFactory.invoke(null);
                Class<?> builderClass = builderInstance.getClass();
                Method buildMethod = builderClass.getMethod("build");

                Map<String, Field> entityFieldsByName = collectEntityFields(entityType);
                List<Field> dtoFields = collectDtoFields(dtoType);

                List<FieldMapping> scalarMappings = new ArrayList<>();
                List<Field> referenceDtoFields = new ArrayList<>();
                List<DtoFieldMapping> dtoFieldMappings = new ArrayList<>();

                for (Field dtoField : dtoFields) {
                    if (dtoField.isAnnotationPresent(EntityReference.class)) {
                        referenceDtoFields.add(dtoField);
                        continue;
                    }
                    if (dtoField.isAnnotationPresent(CacheDtoField.class)) {
                        CacheDtoField dtoFieldAnnotation = dtoField.getAnnotation(CacheDtoField.class);
                        Field entityField = resolveEntityFieldForDtoField(entityType, entityFieldsByName, dtoField, dtoFieldAnnotation);
                        Method factoryMethod = resolveDtoFactoryMethod(dtoField, dtoFieldAnnotation);
                        dtoFieldMappings.add(new DtoFieldMapping(dtoField, entityField, factoryMethod));
                        continue;
                    }
                    Field entityField = entityFieldsByName.get(dtoField.getName());
                    if (entityField == null) {
                        continue;
                    }
                    if (!areTypesCompatible(dtoField.getType(), entityField.getType())) {
                        continue;
                    }
                    Method setter = resolveBuilderSetter(builderClass, entityField.getName(), entityField.getType());
                    scalarMappings.add(new FieldMapping(dtoField, entityField, setter));
                }

                referenceDtoFields.sort(Comparator.<Field>comparingInt(field -> field.getAnnotation(EntityReference.class).order())
                        .thenComparing(Field::getName));

                Set<Field> usedEntityFields = new HashSet<>();
                List<ReferenceMapping> referenceMappings = new ArrayList<>(referenceDtoFields.size());
                for (Field dtoField : referenceDtoFields) {
                    EntityReference reference = dtoField.getAnnotation(EntityReference.class);
                    Field entityField = resolveEntityFieldForReference(entityType, entityFieldsByName, usedEntityFields, dtoField, reference);
                    Method setter = resolveBuilderSetter(builderClass, entityField.getName(), entityField.getType());
                    referenceMappings.add(new ReferenceMapping(dtoField, entityField, setter, reference));
                    usedEntityFields.add(entityField);
                }

                return new MappingMetadata<>(entityType, builderFactory, buildMethod,
                        List.copyOf(scalarMappings), List.copyOf(referenceMappings), List.copyOf(dtoFieldMappings));
            } catch (ReflectiveOperationException ex) {
                throw new IllegalStateException("Failed to analyse DTO mapping for " + dtoType.getSimpleName(), ex);
            }
        }

        private static Map<String, Field> collectEntityFields(Class<?> entityType) {
            Map<String, Field> fields = new HashMap<>();
            Class<?> current = entityType;
            while (current != null && current != Object.class) {
                for (Field field : current.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    field.setAccessible(true);
                    fields.putIfAbsent(field.getName(), field);
                }
                current = current.getSuperclass();
            }
            return fields;
        }

        private static List<Field> collectDtoFields(Class<?> dtoType) {
            List<Field> fields = new ArrayList<>();
            Class<?> current = dtoType;
            while (current != null && current != Object.class) {
                for (Field field : current.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    field.setAccessible(true);
                    fields.add(field);
                }
                current = current.getSuperclass();
            }
            return fields;
        }

        private static Field resolveEntityFieldForReference(Class<?> entityType, Map<String, Field> entityFieldsByName,
                                                             Set<Field> usedEntityFields, Field dtoField,
                                                             EntityReference reference) {
            if (reference.entityType() != Void.class) {
                List<Field> candidates = new ArrayList<>();
                Class<?> current = entityType;
                while (current != null && current != Object.class) {
                    for (Field field : current.getDeclaredFields()) {
                        if (Modifier.isStatic(field.getModifiers())) {
                            continue;
                        }
                        if (reference.entityType().isAssignableFrom(field.getType()) && !usedEntityFields.contains(field)) {
                            field.setAccessible(true);
                            candidates.add(field);
                        }
                    }
                    current = current.getSuperclass();
                }
                if (!candidates.isEmpty()) {
                    return candidates.get(0);
                }
            }

            String candidateName = dtoField.getName();
            if (candidateName.endsWith("Id") && candidateName.length() > 2) {
                candidateName = candidateName.substring(0, candidateName.length() - 2);
            }
            Field fallback = entityFieldsByName.get(candidateName);
            if (fallback != null) {
                return fallback;
            }

            throw new IllegalStateException("Unable to resolve entity field for reference dto field " + dtoField.getName());
        }

        private static Method resolveBuilderSetter(Class<?> builderClass, String fieldName, Class<?> parameterType)
                throws NoSuchMethodException {
            Method[] methods = builderClass.getMethods();
            for (Method method : methods) {
                if (!method.getName().equals(fieldName) || method.getParameterCount() != 1) {
                    continue;
                }
                Class<?> builderParam = method.getParameterTypes()[0];
                if (builderParam.isAssignableFrom(parameterType) || parameterType.isAssignableFrom(builderParam)) {
                    method.setAccessible(true);
                    return method;
                }
            }
            throw new NoSuchMethodException("Builder method not found for field " + fieldName + " on " + builderClass.getName());
        }

        private static Field resolveEntityFieldForDtoField(Class<?> entityType, Map<String, Field> entityFieldsByName,
                                                            Field dtoField, CacheDtoField annotation) {
            Field directMatch = entityFieldsByName.get(dtoField.getName());
            if (directMatch != null && annotation.entityType().isAssignableFrom(directMatch.getType())) {
                return directMatch;
            }

            for (Field field : entityFieldsByName.values()) {
                if (annotation.entityType().isAssignableFrom(field.getType())) {
                    return field;
                }
            }

            throw new IllegalStateException("Unable to resolve entity field for DTO field " + dtoField.getName());
        }

        private static Method resolveDtoFactoryMethod(Field dtoField, CacheDtoField annotation) throws NoSuchMethodException {
            Class<?> dtoType = dtoField.getType();
            Method factoryMethod = dtoType.getMethod(annotation.factoryMethod(), annotation.entityType());
            if (!Modifier.isStatic(factoryMethod.getModifiers())) {
                throw new NoSuchMethodException("Factory method " + annotation.factoryMethod() + " on " + dtoType.getName() + " must be static");
            }
            if (!dtoType.isAssignableFrom(factoryMethod.getReturnType())) {
                throw new NoSuchMethodException("Factory method " + factoryMethod + " must return " + dtoType.getName());
            }
            factoryMethod.setAccessible(true);
            return factoryMethod;
        }

        private Object newBuilderInstance() {
            try {
                return builderFactory.invoke(null);
            } catch (ReflectiveOperationException ex) {
                throw new IllegalStateException("Unable to obtain builder for " + entityType.getSimpleName(), ex);
            }
        }

        private static boolean areTypesCompatible(Class<?> dtoType, Class<?> entityType) {
            Class<?> left = wrap(dtoType);
            Class<?> right = wrap(entityType);
            return left.isAssignableFrom(right) || right.isAssignableFrom(left);
        }

        private static Class<?> wrap(Class<?> type) {
            if (type == null || !type.isPrimitive()) {
                return type;
            }
            if (type == boolean.class) return Boolean.class;
            if (type == byte.class) return Byte.class;
            if (type == short.class) return Short.class;
            if (type == char.class) return Character.class;
            if (type == int.class) return Integer.class;
            if (type == long.class) return Long.class;
            if (type == float.class) return Float.class;
            if (type == double.class) return Double.class;
            return type;
        }

        void copyEntityToDto(E entity, EntityBackedCacheDto<ID, E> dto) {
            try {
                for (FieldMapping mapping : scalarMappings) {
                    Object value = mapping.entityField.get(entity);
                    mapping.dtoField.set(dto, value);
                }

                for (ReferenceMapping mapping : referenceMappings) {
                    Object related = mapping.entityField.get(entity);
                    if (related == null) {
                        if (!mapping.reference.optional()) {
                            throw new IllegalStateException("Missing required relation for field " + mapping.dtoField.getName());
                        }
                        mapping.dtoField.set(dto, null);
                        continue;
                    }

                    Field idField = resolveEntityIdField(related.getClass());
                    Object idValue = idField.get(related);
                    mapping.dtoField.set(dto, idValue);
                }

                for (DtoFieldMapping mapping : dtoFieldMappings) {
                    Object relatedEntity = mapping.entityField.get(entity);
                    if (relatedEntity == null) {
                        mapping.dtoField.set(dto, null);
                        continue;
                    }
                    Object convertedDto = mapping.factoryMethod.invoke(null, relatedEntity);
                    mapping.dtoField.set(dto, convertedDto);
                }
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException("Failed to copy entity to DTO", ex);
            } catch (ReflectiveOperationException ex) {
                throw new IllegalStateException("Failed to convert nested entity to DTO", ex);
            }
        }

        @SuppressWarnings("unchecked")
        E buildEntityFromDto(EntityBackedCacheDto<ID, E> dto, Object[] relatedEntities) {
            Object builder = newBuilderInstance();
            try {
                for (FieldMapping mapping : scalarMappings) {
                    Object value = mapping.dtoField.get(dto);
                    mapping.builderSetter.invoke(builder, value);
                }

                Object[] varArgs = relatedEntities != null ? relatedEntities : new Object[0];
                int supplied = varArgs.length;
                for (int index = 0; index < referenceMappings.size(); index++) {
                    ReferenceMapping mapping = referenceMappings.get(index);
                    Object related = index < supplied ? varArgs[index] : null;
                    if (related == null && !mapping.reference.optional()) {
                        throw new IllegalStateException("Missing required related entity for " + mapping.dtoField.getName());
                    }
                    mapping.builderSetter.invoke(builder, related);
                }

                return (E) buildMethod.invoke(builder);
            } catch (ReflectiveOperationException ex) {
                throw new IllegalStateException("Failed to construct entity from DTO", ex);
            }
        }
    }
}
