package com.example.planmate.domain.shared.realtime.meta;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.Attribute.PersistentAttributeType;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RelationshipInspector {

    private final EntityManager em;

    public Map<String, RelationMeta> inspect(Class<?> entityClass) {
        Metamodel mm = em.getMetamodel();
        EntityType<?> et = mm.entity(entityClass);

        Map<String, RelationMeta> map = new HashMap<>();
        for (Attribute<?, ?> attr : et.getAttributes()) {
            PersistentAttributeType t = attr.getPersistentAttributeType();
            boolean isRelation = t == PersistentAttributeType.ONE_TO_MANY
                    || t == PersistentAttributeType.MANY_TO_ONE
                    || t == PersistentAttributeType.ONE_TO_ONE
                    || t == PersistentAttributeType.MANY_TO_MANY;
            if (!isRelation) continue;

            Class<?> relatedType;
            boolean collection = attr.isCollection();
            if (collection) {
                PluralAttribute<?, ?, ?> pa = (PluralAttribute<?, ?, ?>) attr;
                relatedType = pa.getElementType().getJavaType();
            } else {
                SingularAttribute<?, ?> sa = (SingularAttribute<?, ?>) attr;
                relatedType = sa.getType().getJavaType();
            }
            map.put(attr.getName(), new RelationMeta(attr.getName(), t, relatedType, collection));
        }
        return map;
    }

    public record RelationMeta(String name, PersistentAttributeType kind, Class<?> relatedClass, boolean collection) {}
}
