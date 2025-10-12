package com.example.planmate.domain.framework.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SocketRoot {
    /** STOMP topic prefix, e.g. "plan", "travel" */
    String topic();
}