package com.sharedsync.framework.shared.presence.core;

import com.sharedsync.framework.shared.presence.annotation.PresenceRoot;
import jakarta.annotation.PostConstruct;
import org.reflections.Reflections;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class PresenceRootResolver {

    private String channelName;
    private Class<?> rootType;

    @PostConstruct
    public void init() {
        Reflections reflections = new Reflections("com.example");

        Set<Class<?>> roots = reflections.getTypesAnnotatedWith(PresenceRoot.class);

        if (roots.isEmpty())
            throw new IllegalStateException("No @PresenceRoot found");

        rootType = roots.iterator().next();
        PresenceRoot ann = rootType.getAnnotation(PresenceRoot.class);
        channelName = ann.channel();
    }

    public String getChannel() { return channelName; }
    public Class<?> getRootType() { return rootType; }
}
