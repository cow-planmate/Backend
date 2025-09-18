package com.example.planmate.infrastructure.redis;

/**
 * Central place for Redis key prefixes & small helpers.
 * Keep suffix (id) concatenation simple to avoid accidental duplicate separators.
 */
public final class RedisKeys {
    private RedisKeys() {}

    public static final String PLAN = "PLAN"; // + planId
    public static final String TIMETABLE = "TIMETABLE"; // + timeTableId
    public static final String PLAN_TO_TIMETABLE = "PLANTOTIMETABLE"; // + planId -> List<Integer>
    public static final String TIMETABLE_PLACE_BLOCK = "TIMETABLEPLACEBLOCK"; // + blockId
    public static final String TIMETABLE_TO_BLOCK = "TIMETABLETOTIMETABLEPLACEBLOCK"; // + timetableId -> List<Integer>
    public static final String TRAVEL = "TRAVEL"; // + travelId
    public static final String PLACE_CATEGORY = "PLACECATEGORY"; // + placeCategoryId
    public static final String USERID_NICKNAME = "USERIDNICKNAME"; // + userId
    public static final String NICKNAME_USERID = "NICKNAMEUSERID"; // + nickname
    public static final String PLAN_TRACKER = "PLANTRACKER"; // + planId (hash field: userId -> dayIndex)
    public static final String USERID_TO_PLANID = "USERIDTOPLANID"; // + userId -> planId
    public static final String REFRESH_TOKEN = "REFRESHTOKEN"; // + token

    public static String plan(int planId){ return PLAN + planId; }
    public static String timeTable(int id){ return TIMETABLE + id; }
    public static String planToTimeTable(int planId){ return PLAN_TO_TIMETABLE + planId; }
    public static String timeTableBlock(int id){ return TIMETABLE_PLACE_BLOCK + id; }
    public static String timeTableToBlocks(int ttId){ return TIMETABLE_TO_BLOCK + ttId; }
    public static String travel(int travelId){ return TRAVEL + travelId; }
    public static String placeCategory(int id){ return PLACE_CATEGORY + id; }
    public static String userNickname(int userId){ return USERID_NICKNAME + userId; }
    public static String nicknameUser(String nickname){ return NICKNAME_USERID + nickname; }
    public static String planTracker(int planId){ return PLAN_TRACKER + planId; }
    public static String userPlan(int userId){ return USERID_TO_PLANID + userId; }
    public static String refreshToken(String token){ return REFRESH_TOKEN + token; }
}
