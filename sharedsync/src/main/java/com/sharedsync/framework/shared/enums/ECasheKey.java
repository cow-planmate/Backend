package com.sharedsync.framework.shared.enums;

public enum ECasheKey {
    // 특수 용도 키들 (수동 관리)
    PLANTOTIMETABLE("PLANTOTIMETABLE"),
    TIMETABLETOTIMETABLEPLACEBLOCK("TIMETABLETOTIMETABLEPLACEBLOCK"),
    TRAVEL("TRAVEL"),
    PLACECATEGORY("PLACECATEGORY"),
    USERIDNICKNAME("USERIDNICKNAME"),
    NICKNAMEUSERID("NICKNAMEUSERID"),
    PLANTRACKER("PLANTRACKER"),
    USERIDTOPLANID("USERIDTOPLANID"),
    REFRESHTOKEN("REFRESHTOKEN");

    private final String value;

    ECasheKey(String value) { this.value = value; }

    public String key(Object suffix) {
        return value + suffix;
    }
    
    public String getValue() {
        return value;
    }
}