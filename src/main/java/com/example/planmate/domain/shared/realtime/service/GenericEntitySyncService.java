package com.example.planmate.domain.shared.realtime.service;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.planmate.domain.shared.realtime.dto.EntityPatch;
import com.example.planmate.domain.shared.realtime.dto.RelationDelta;
import com.example.planmate.domain.shared.realtime.meta.RelationshipInspector;
import com.example.planmate.domain.shared.realtime.meta.RelationshipInspector.RelationMeta;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.metamodel.SingularAttribute;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GenericEntitySyncService {

    private final EntityManager em;
    private final RelationshipInspector inspector;

    @Transactional
    public Object applyPatch(EntityPatch patch) {
        PatchContext ctx = new PatchContext(patch);
        new Pipeline()
            .add(new LoadEntityStep())
            .add(new ApplyAttributesStep())
            .add(new ApplyRelationsStep())
            // .add(new ValidateBusinessRulesStep()) // 필요 시 추가
            .run(ctx);
        return ctx.entity;
    }

    private void applyAttributes(Object entity, Map<String, Object> attributes) {
        for (Map.Entry<String, Object> e : attributes.entrySet()) {
            setProperty(entity, e.getKey(), e.getValue());
        }
    }

    private void applyRelations(Class<?> entityClass, Object entity, Map<String, RelationDelta> relations) {
        Map<String, RelationMeta> meta = inspector.inspect(entityClass);
        relationshipsLoop:
        for (Map.Entry<String, RelationDelta> e : relations.entrySet()) {
            RelationMeta rm = meta.get(e.getKey());
            if (rm == null) continue relationshipsLoop; // unknown relation: ignore or throw

            if (!rm.collection()) {
                // to-one
                Object ref = e.getValue().getSet() == null ? null : em.getReference(rm.relatedClass(), e.getValue().getSet());
                setProperty(entity, rm.name(), ref);
                // bidirectional maintenance can be added here if needed
            } else {
                // to-many: compute delta
                Collection<Object> current = (Collection<Object>) getProperty(entity, rm.name());
                Set<Object> currentIds = current.stream().map(this::getIdValue).filter(Objects::nonNull).collect(Collectors.toSet());

                Set<Object> targetIds;
                if (e.getValue().getSetAll() != null) {
                    targetIds = new HashSet<>(e.getValue().getSetAll());
                } else {
                    targetIds = new HashSet<>(currentIds);
                    if (e.getValue().getRemove() != null) targetIds.removeAll(e.getValue().getRemove());
                    if (e.getValue().getAdd() != null) targetIds.addAll(e.getValue().getAdd());
                }

                Set<Object> toRemove = new HashSet<>(currentIds);
                toRemove.removeAll(targetIds);
                Set<Object> toAdd = new HashSet<>(targetIds);
                toAdd.removeAll(currentIds);

                if (!toRemove.isEmpty()) {
                    current.removeIf(e2 -> toRemove.contains(getIdValue(e2)));
                }
                for (Object addId : toAdd) {
                    Object ref = em.getReference(rm.relatedClass(), addId);
                    current.add(ref);
                }
                // optional: maintain bidirectional side
            }
        }
    }

    // Utilities
    private void setProperty(Object bean, String name, Object value) {
        PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(bean.getClass(), name);
        if (pd == null || pd.getWriteMethod() == null) {
            throw new IllegalArgumentException("No writable property '" + name + "' on " + bean.getClass().getSimpleName());
        }
        try {
            pd.getWriteMethod().invoke(bean, value);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to write property '" + name + "'", ex);
        }
    }

    private Object getProperty(Object bean, String name) {
        PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(bean.getClass(), name);
        if (pd == null || pd.getReadMethod() == null) {
            throw new IllegalArgumentException("No readable property '" + name + "' on " + bean.getClass().getSimpleName());
        }
        try {
            return pd.getReadMethod().invoke(bean);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to read property '" + name + "'", ex);
        }
    }

    private Object getIdValue(Object entity) {
        Metamodel mm = em.getMetamodel();
        EntityType<?> et = mm.entity(entity.getClass());
        SingularAttribute<?, ?> idAttr = et.getId(et.getIdType().getJavaType());
        return readAttribute(entity, idAttr);
    }

    private Object readAttribute(Object entity, Attribute<?, ?> attr) {
        PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(entity.getClass(), attr.getName());
        if (pd == null || pd.getReadMethod() == null) return null;
        try {
            return pd.getReadMethod().invoke(entity);
        } catch (Exception ex) {
            return null;
        }
    }

    private Class<?> resolveClass(String nameOrFqcn) {
        try {
            return Class.forName(nameOrFqcn);
        } catch (ClassNotFoundException e) {
            // fallback: resolve by JPA entity name
            for (EntityType<?> type : em.getMetamodel().getEntities()) {
                if (nameOrFqcn.equals(type.getName())) {
                    return type.getJavaType();
                }
            }
            throw new IllegalArgumentException("Unknown entity: " + nameOrFqcn);
        }
    }

    // ===== Inline lightweight framework (no new files) =====
    private static final class PatchContext {
        final EntityPatch patch;
        Class<?> entityClass;
        Object entity;
        PatchContext(EntityPatch patch) { this.patch = patch; }
    }

    private interface Step { void execute(PatchContext ctx); }

    private static final class Pipeline {
        private final List<Step> steps = new ArrayList<>();
        Pipeline add(Step step) { steps.add(step); return this; }
        void run(PatchContext ctx) { steps.forEach(s -> s.execute(ctx)); }
    }

    private final class LoadEntityStep implements Step {
        @Override public void execute(PatchContext ctx) {
            ctx.entityClass = resolveClass(ctx.patch.getEntity());
            ctx.entity = em.find(ctx.entityClass, ctx.patch.getId(), LockModeType.OPTIMISTIC);
            if (ctx.entity == null) throw new IllegalArgumentException(
                "Entity not found: " + ctx.patch.getEntity() + " id=" + ctx.patch.getId());
        }
    }

    private final class ApplyAttributesStep implements Step {
        @Override public void execute(PatchContext ctx) {
            Map<String, Object> attrs = ctx.patch.getAttributes();
            if (attrs != null && !attrs.isEmpty()) {
                applyAttributes(ctx.entity, attrs);
            }
        }
    }

    private final class ApplyRelationsStep implements Step {
        @Override public void execute(PatchContext ctx) {
            Map<String, RelationDelta> rels = ctx.patch.getRelations();
            if (rels != null && !rels.isEmpty()) {
                applyRelations(ctx.entityClass, ctx.entity, rels);
            }
        }
    }
}
