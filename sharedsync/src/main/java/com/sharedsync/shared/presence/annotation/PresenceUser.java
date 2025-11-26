package com.sharedsync.shared.presence.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * PresenceUser
 *
 * User 엔티티를 Presence 시스템과 연결하기 위한 어노테이션.
 * 프레임워크는 해당 어노테이션이 달린 엔티티를 스캔하여
 * userId 및 nickname 필드를 자동 인식한다.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PresenceUser {
    /**
     * 유저 식별자 필드 이름 (예: "userId")
     */
    String idField();

    /**
     * 유저 닉네임 필드 이름 (예: "nickname")
     */
    String nameField();
}
