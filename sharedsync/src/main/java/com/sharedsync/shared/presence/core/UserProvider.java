package com.sharedsync.shared.presence.core;

/**
 * 앱이 구현하는 유저 정보 조회 인터페이스
 * (나중엔 코드 생성기로 자동 생성될 예정)
 */
public interface UserProvider {
    /**
     * userId로부터 닉네임을 조회한다.
     * DB나 캐시 접근은 앱에서 자유롭게 구현.
     */
    String findNicknameByUserId(int userId);
}
