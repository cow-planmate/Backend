package com.example.planmate.domain.webSocket.enums;

public enum ECasheKey {
    PLAN("PLAN"),
    TIMETABLE("TIMETABLE"),
    PLANTOTIMETABLE("PLANTOTIMETABLE"),
    TIMETABLEPLACEBLOCK("TIMETABLEPLACEBLOCK"),
    TIMETABLETOTIMETABLEPLACEBLOCK("TIMETABLETOTIMETABLEPLACEBLOCK"),
    TRAVEL("TRAVEL"),
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
}