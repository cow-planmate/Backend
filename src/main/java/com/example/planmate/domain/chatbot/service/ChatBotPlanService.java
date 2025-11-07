package com.example.planmate.domain.chatbot.service;

import com.example.planmate.common.valueObject.TimetablePlaceBlockVO;
import com.example.planmate.common.valueObject.TimetableVO;
import com.example.planmate.domain.chatbot.dto.ChatBotActionResponse;
import com.example.planmate.domain.webSocket.dto.WPlanRequest;
import com.example.planmate.domain.webSocket.dto.WTimeTablePlaceBlockRequest;
import com.example.planmate.domain.webSocket.dto.WTimetableRequest;
import com.example.planmate.domain.webSocket.service.WebSocketPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * AI 챗봇이 호출할 수 있는 여행 계획 관련 함수들을 정의
 * 사용자에게는 친근한 메시지, 시스템에게는 구조화된 액션 반환
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatBotPlanService {
    
    private final WebSocketPlanService webSocketPlanService;
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * 계획 이름 변경
     */
    public ChatBotActionResponse changePlanName(int planId, String planName) {
        try {
            log.info("AI 요청: 계획 이름 변경 - planId: {}, planName: {}", planId, planName);
            
            // 액션 데이터 구성
            WPlanRequest request = new WPlanRequest();
            request.setPlanName(planName);
            
            // 사용자 메시지
            String userMessage = "여행 계획 이름을 '" + planName + "'으로 변경했습니다! ✅";
            
            return ChatBotActionResponse.withAction(userMessage, "update", "plan", request);
            
        } catch (Exception e) {
            log.error("계획 이름 변경 실패: {}", e.getMessage());
            return ChatBotActionResponse.simpleMessage("계획 이름 변경에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 출발지 변경
     */
    public ChatBotActionResponse changeDeparture(int planId, String departure) {
        try {
            log.info("AI 요청: 출발지 변경 - planId: {}, departure: {}", planId, departure);
            
            WPlanRequest request = new WPlanRequest();
            request.setDeparture(departure);
            
            String userMessage = "출발지를 '" + departure + "'로 변경했습니다! 🚗";
            
            return ChatBotActionResponse.withAction(userMessage, "update", "plan", request);
            
        } catch (Exception e) {
            log.error("출발지 변경 실패: {}", e.getMessage());
            return ChatBotActionResponse.simpleMessage("출발지 변경에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 인원 수 변경
     */
    public ChatBotActionResponse changePersonCount(int planId, Integer adultCount, Integer childCount) {
        try {
            log.info("AI 요청: 인원 수 변경 - planId: {}, adult: {}, child: {}", 
                    planId, adultCount, childCount);
            
            WPlanRequest request = new WPlanRequest();
            if (adultCount != null) request.setAdultCount(adultCount);
            if (childCount != null) request.setChildCount(childCount);
            
            String userMessage = "인원 수를 변경했습니다! 👥";
            if (adultCount != null) userMessage += " 성인: " + adultCount + "명";
            if (childCount != null) userMessage += " 어린이: " + childCount + "명";
            
            return ChatBotActionResponse.withAction(userMessage, "update", "plan", request);
            
        } catch (Exception e) {
            log.error("인원 수 변경 실패: {}", e.getMessage());
            return ChatBotActionResponse.simpleMessage("인원 수 변경에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 교통수단 변경
     */
    public ChatBotActionResponse changeTransportation(int planId, int transportationId) {
        try {
            log.info("AI 요청: 교통수단 변경 - planId: {}, transportationId: {}", 
                    planId, transportationId);
            
            WPlanRequest request = new WPlanRequest();
            request.setTransportationCategoryId(transportationId);
            
            String transportName = getTransportationName(transportationId);
            String userMessage = "교통수단을 '" + transportName + "'로 변경했습니다! " + getTransportationEmoji(transportationId);
            
            return ChatBotActionResponse.withAction(userMessage, "update", "plan", request);
            
        } catch (Exception e) {
            log.error("교통수단 변경 실패: {}", e.getMessage());
            return ChatBotActionResponse.simpleMessage("교통수단 변경에 실패했습니다: " + e.getMessage());
        }
    }
    
    private String getTransportationName(int id) {
        return switch (id) {
            case 1 -> "도보";
            case 2 -> "자전거";
            case 3 -> "자동차";
            case 4 -> "대중교통";
            default -> "알 수 없음";
        };
    }
    
    private String getTransportationEmoji(int id) {
        return switch (id) {
            case 1 -> "🚶";
            case 2 -> "🚴";
            case 3 -> "🚗";
            case 4 -> "🚌";
            default -> "🚀";
        };
    }
    
    // ===== TimeTable 관련 메소드들 =====
    
    /**
     * 타임테이블 생성
     */
    public ChatBotActionResponse createTimetable(int planId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        try {
            log.info("AI 요청: 타임테이블 생성 - planId: {}, date: {}", planId, date);
            
            TimetableVO timetableVO = new TimetableVO();
            timetableVO.setDate(date);
            timetableVO.setStartTime(startTime);
            timetableVO.setEndTime(endTime);
            
            WTimetableRequest request = new WTimetableRequest();
            request.getTimetableVOs().add(timetableVO);
            
            String userMessage = String.format("%s 날짜의 새로운 일정을 생성했습니다! 📅", date.toString());
            
            return ChatBotActionResponse.withAction(userMessage, "create", "timeTable", request);
            
        } catch (Exception e) {
            log.error("타임테이블 생성 실패: {}", e.getMessage());
            return ChatBotActionResponse.simpleMessage("타임테이블 생성에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 타임테이블 수정
     */
    public ChatBotActionResponse updateTimetable(int timetableId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        try {
            log.info("AI 요청: 타임테이블 수정 - timetableId: {}, date: {}", timetableId, date);
            
            TimetableVO timetableVO = new TimetableVO();
            timetableVO.setTimetableId(timetableId);
            timetableVO.setDate(date);
            timetableVO.setStartTime(startTime);
            timetableVO.setEndTime(endTime);
            
            WTimetableRequest request = new WTimetableRequest();
            request.getTimetableVOs().add(timetableVO);
            
            String userMessage = String.format("일정을 %s로 수정했습니다! ✏️", date.toString());
            
            return ChatBotActionResponse.withAction(userMessage, "update", "timeTable", request);
            
        } catch (Exception e) {
            log.error("타임테이블 수정 실패: {}", e.getMessage());
            return ChatBotActionResponse.simpleMessage("타임테이블 수정에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 타임테이블 삭제
     */
    public ChatBotActionResponse deleteTimetable(int timetableId) {
        try {
            log.info("AI 요청: 타임테이블 삭제 - timetableId: {}", timetableId);
            
            TimetableVO timetableVO = new TimetableVO();
            timetableVO.setTimetableId(timetableId);
            
            WTimetableRequest request = new WTimetableRequest();
            request.getTimetableVOs().add(timetableVO);
            
            String userMessage = "일정을 삭제했습니다! 🗑️";
            
            return ChatBotActionResponse.withAction(userMessage, "delete", "timeTable", request);
            
        } catch (Exception e) {
            log.error("타임테이블 삭제 실패: {}", e.getMessage());
            return ChatBotActionResponse.simpleMessage("타임테이블 삭제에 실패했습니다: " + e.getMessage());
        }
    }
    
    // ===== TimeTablePlaceBlock 관련 메소드들 =====
    
    /**
     * 장소 블록 생성
     */
    public ChatBotActionResponse createPlaceBlock(int timetableId, String placeName, String placeAddress, 
                                                 LocalTime startTime, LocalTime endTime, Float rating) {
        try {
            log.info("AI 요청: 장소 블록 생성 - timetableId: {}, placeName: {}", timetableId, placeName);
            
            TimetablePlaceBlockVO placeBlockVO = new TimetablePlaceBlockVO(
                timetableId, null, null, placeName, rating, placeAddress, null, null, null, startTime, endTime, null, null
            );
            
            WTimeTablePlaceBlockRequest request = new WTimeTablePlaceBlockRequest();
            request.setTimetablePlaceBlockVO(placeBlockVO);
            
            String userMessage = String.format("'%s' 장소를 일정에 추가했습니다! 📍", placeName);
            
            return ChatBotActionResponse.withAction(userMessage, "create", "timeTablePlaceBlock", request);
            
        } catch (Exception e) {
            log.error("장소 블록 생성 실패: {}", e.getMessage());
            return ChatBotActionResponse.simpleMessage("장소 추가에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 장소 블록 수정
     */
    public ChatBotActionResponse updatePlaceBlock(int blockId, String placeName, String placeAddress, 
                                                 LocalTime startTime, LocalTime endTime, Float rating) {
        try {
            log.info("AI 요청: 장소 블록 수정 - blockId: {}, placeName: {}", blockId, placeName);
            
            TimetablePlaceBlockVO placeBlockVO = new TimetablePlaceBlockVO(
                0, blockId, null, placeName, rating, placeAddress, null, null, null, startTime, endTime, null, null
            );
            
            WTimeTablePlaceBlockRequest request = new WTimeTablePlaceBlockRequest();
            request.setTimetablePlaceBlockVO(placeBlockVO);
            
            String userMessage = String.format("'%s' 장소 정보를 수정했습니다! ✏️", placeName);
            
            return ChatBotActionResponse.withAction(userMessage, "update", "timeTablePlaceBlock", request);
            
        } catch (Exception e) {
            log.error("장소 블록 수정 실패: {}", e.getMessage());
            return ChatBotActionResponse.simpleMessage("장소 수정에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 장소 블록 삭제
     */
    public ChatBotActionResponse deletePlaceBlock(int timetableId, int blockId) {
        try {
            log.info("AI 요청: 장소 블록 삭제 - blockId: {}", blockId);
            
            TimetablePlaceBlockVO placeBlockVO = new TimetablePlaceBlockVO(
                timetableId, blockId, null, null, null, null, null, null, null, null, null, null, null
            );
            
            WTimeTablePlaceBlockRequest request = new WTimeTablePlaceBlockRequest();
            request.setTimetablePlaceBlockVO(placeBlockVO);
            
            String userMessage = "장소를 일정에서 제거했습니다! 🗑️";
            
            return ChatBotActionResponse.withAction(userMessage, "delete", "timeTablePlaceBlock", request);
            
        } catch (Exception e) {
            log.error("장소 블록 삭제 실패: {}", e.getMessage());
            return ChatBotActionResponse.simpleMessage("장소 삭제에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 전체 계획 정보 업데이트 (JSON 형태로 받은 모든 필드를 처리)
     */
    public ChatBotActionResponse updateFullPlan(int planId, String planJson) {
        try {
            log.info("AI 요청: 전체 계획 업데이트 - planId: {}, planData: {}", planId, planJson);
            
            // JSON을 Map으로 파싱
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> planMap = objectMapper.readValue(planJson, java.util.Map.class);
            
            // WPlanRequest 객체 생성 및 필드 설정
            WPlanRequest request = new WPlanRequest();
            
            if (planMap.containsKey("planName")) {
                request.setPlanName((String) planMap.get("planName"));
            }
            if (planMap.containsKey("departure")) {
                request.setDeparture((String) planMap.get("departure"));
            }
            if (planMap.containsKey("adultCount")) {
                request.setAdultCount((Integer) planMap.get("adultCount"));
            }
            if (planMap.containsKey("childCount")) {
                request.setChildCount((Integer) planMap.get("childCount"));
            }
            if (planMap.containsKey("transportationCategoryId")) {
                request.setTransportationCategoryId((Integer) planMap.get("transportationCategoryId"));
            }
            if(planMap.containsKey("travelId")) {
                request.setTravelId((Integer) planMap.get("travelId"));
            }
            
            // 사용자 메시지 생성
            StringBuilder messageBuilder = new StringBuilder("여행 계획을 업데이트했습니다! ✅\n");
            
            if (planMap.containsKey("planName")) {
                messageBuilder.append("• 계획 이름: ").append(planMap.get("planName")).append("\n");
            }
            if (planMap.containsKey("departure")) {
                messageBuilder.append("• 출발지: ").append(planMap.get("departure")).append("\n");
            }
            if (planMap.containsKey("adultCount") || planMap.containsKey("childCount")) {
                messageBuilder.append("• 인원: ");
                if (planMap.containsKey("adultCount")) {
                    messageBuilder.append("성인 ").append(planMap.get("adultCount")).append("명");
                }
                if (planMap.containsKey("childCount")) {
                    Integer childCount = (Integer) planMap.get("childCount");
                    if (childCount != null && childCount > 0) {
                        messageBuilder.append(", 아이 ").append(childCount).append("명");
                    }
                }
                messageBuilder.append("\n");
            }
            if (planMap.containsKey("transportationCategoryId")) {
                messageBuilder.append("• 교통수단 ID: ").append(planMap.get("transportationCategoryId")).append("\n");
            }
            
            String userMessage = messageBuilder.toString().trim();
            
            return ChatBotActionResponse.withAction(userMessage, "update", "plan", request);
            
        } catch (Exception e) {
            log.error("전체 계획 업데이트 실패: {}", e.getMessage());
            return ChatBotActionResponse.simpleMessage("계획 업데이트에 실패했습니다: " + e.getMessage());
        }
    }
    
    // ============== TimeTable CRUD 메서드들 ==============
    
    /**
     * 타임테이블 생성
     */
    public ChatBotActionResponse createTimeTable(int planId, String timeTableJson) {
        try {
            log.info("AI 요청: 타임테이블 생성 - planId: {}, timeTableData: {}", planId, timeTableJson);
            
            // JSON을 Map으로 파싱
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> timeTableMap = objectMapper.readValue(timeTableJson, java.util.Map.class);
            
            // WTimetableRequest 객체 생성
            WTimetableRequest request = new WTimetableRequest();
            TimetableVO timetableVO = new TimetableVO();
            
            if (timeTableMap.containsKey("date")) {
                String dateStr = (String) timeTableMap.get("date");
                timetableVO.setDate(LocalDate.parse(dateStr));
            }
            
            // startTime 처리 - 없으면 기본값 09:00
            if (timeTableMap.containsKey("startTime")) {
                String startTimeStr = (String) timeTableMap.get("startTime");
                timetableVO.setStartTime(LocalTime.parse(startTimeStr));
            } else {
                timetableVO.setStartTime(LocalTime.of(9, 0)); // 기본값: 09:00
            }
            
            // endTime 처리 - 없으면 기본값 20:00
            if (timeTableMap.containsKey("endTime")) {
                String endTimeStr = (String) timeTableMap.get("endTime");
                timetableVO.setEndTime(LocalTime.parse(endTimeStr));
            } else {
                timetableVO.setEndTime(LocalTime.of(20, 0)); // 기본값: 20:00
            }
            
            request.setTimetableVOs(java.util.List.of(timetableVO));
            
            String userMessage = "새로운 타임테이블을 생성했습니다! 📅";
            
            return ChatBotActionResponse.withAction(userMessage, "create", "timeTable", request);
            
        } catch (Exception e) {
            log.error("타임테이블 생성 실패: {}", e.getMessage());
            return ChatBotActionResponse.simpleMessage("타임테이블 생성에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 타임테이블 업데이트
     */
    public ChatBotActionResponse updateTimeTable(Integer timeTableId, String timeTableJson) {
        try {
            log.info("AI 요청: 타임테이블 업데이트 - timeTableId: {}, timeTableData: {}", timeTableId, timeTableJson);
            
            // JSON을 Map으로 파싱
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> timeTableMap = objectMapper.readValue(timeTableJson, java.util.Map.class);
            
            // WTimetableRequest 객체 생성
            WTimetableRequest request = new WTimetableRequest();
            TimetableVO timetableVO = new TimetableVO();
            
            timetableVO.setTimetableId(timeTableId);
            
            if (timeTableMap.containsKey("date")) {
                String dateStr = (String) timeTableMap.get("date");
                timetableVO.setDate(LocalDate.parse(dateStr));
            }
            
            // startTime 처리 - 없으면 기본값 09:00
            if (timeTableMap.containsKey("startTime")) {
                String startTimeStr = (String) timeTableMap.get("startTime");
                timetableVO.setStartTime(LocalTime.parse(startTimeStr));
            } else {
                timetableVO.setStartTime(LocalTime.of(9, 0)); // 기본값: 09:00
            }
            
            // endTime 처리 - 없으면 기본값 20:00
            if (timeTableMap.containsKey("endTime")) {
                String endTimeStr = (String) timeTableMap.get("endTime");
                timetableVO.setEndTime(LocalTime.parse(endTimeStr));
            } else {
                timetableVO.setEndTime(LocalTime.of(20, 0)); // 기본값: 20:00
            }
            
            request.setTimetableVOs(java.util.List.of(timetableVO));
            
            String userMessage = "타임테이블을 업데이트했습니다! ✏️";
            
            return ChatBotActionResponse.withAction(userMessage, "update", "timeTable", request);
            
        } catch (Exception e) {
            log.error("타임테이블 업데이트 실패: {}", e.getMessage());
            return ChatBotActionResponse.simpleMessage("타임테이블 업데이트에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 타임테이블 삭제
     */
    public ChatBotActionResponse deleteTimeTable(Integer timeTableId) {
        try {
            log.info("AI 요청: 타임테이블 삭제 - timeTableId: {}", timeTableId);
            
            // WTimetableRequest 객체 생성
            WTimetableRequest request = new WTimetableRequest();
            TimetableVO timetableVO = new TimetableVO();
            timetableVO.setTimetableId(timeTableId);
            request.setTimetableVOs(java.util.List.of(timetableVO));
            
            String userMessage = "타임테이블을 삭제했습니다! 🗑️";
            
            return ChatBotActionResponse.withAction(userMessage, "delete", "timeTable", request);
            
        } catch (Exception e) {
            log.error("타임테이블 삭제 실패: {}", e.getMessage());
            return ChatBotActionResponse.simpleMessage("타임테이블 삭제에 실패했습니다: " + e.getMessage());
        }
    }
    
    // ============== TimeTablePlaceBlock CRUD 메서드들 ==============
    
    /**
     * 장소 블록 생성
     */
    public ChatBotActionResponse createTimeTablePlaceBlock(Integer timeTableId, String placeBlockJson) {
        try {
            log.info("AI 요청: 장소 블록 생성 - timeTableId: {}, placeBlockData: {}", timeTableId, placeBlockJson);
            
            // JSON을 Map으로 파싱
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> placeBlockMap = objectMapper.readValue(placeBlockJson, java.util.Map.class);
            
            // WTimeTablePlaceBlockRequest 객체 생성
            WTimeTablePlaceBlockRequest request = new WTimeTablePlaceBlockRequest();
            
            // TimetablePlaceBlockVO의 AllArgsConstructor를 사용해서 생성
            TimetablePlaceBlockVO placeBlockVO = new TimetablePlaceBlockVO(
                timeTableId,
                null, // timetablePlaceBlockId - 생성 시에는 null
                (Integer) placeBlockMap.get("placeCategoryId"),
                (String) placeBlockMap.get("placeName"),
                (Float) placeBlockMap.get("placeRating"),
                (String) placeBlockMap.get("placeAddress"),
                (String) placeBlockMap.get("placeLink"),
                (String) placeBlockMap.get("placeId"),
                (String) placeBlockMap.get("date"),
                placeBlockMap.containsKey("startTime") ? LocalTime.parse((String) placeBlockMap.get("startTime")) : LocalTime.parse(placeBlockJson),
                placeBlockMap.containsKey("endTime") ? LocalTime.parse((String) placeBlockMap.get("endTime")) : null,
                (Double) placeBlockMap.get("xLocation"),
                (Double) placeBlockMap.get("yLocation")
            );
            
            request.setTimetablePlaceBlockVO(placeBlockVO);
            
            String userMessage = "새로운 장소를 일정에 추가했습니다! 📍";
            
            return ChatBotActionResponse.withAction(userMessage, "create", "timeTablePlaceBlock", request);
            
        } catch (Exception e) {
            log.error("장소 블록 생성 실패: {}", e.getMessage());
            return ChatBotActionResponse.simpleMessage("장소 추가에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 장소 블록 업데이트
     */
    public ChatBotActionResponse updateTimeTablePlaceBlock(Integer placeBlockId, String placeBlockJson) {
        try {
            log.info("AI 요청: 장소 블록 업데이트 - placeBlockId: {}, placeBlockData: {}", placeBlockId, placeBlockJson);
            
            // JSON을 Map으로 파싱
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> placeBlockMap = objectMapper.readValue(placeBlockJson, java.util.Map.class);
            
            // WTimeTablePlaceBlockRequest 객체 생성
            WTimeTablePlaceBlockRequest request = new WTimeTablePlaceBlockRequest();
            
            // TimetablePlaceBlockVO의 AllArgsConstructor를 사용해서 생성
            TimetablePlaceBlockVO placeBlockVO = new TimetablePlaceBlockVO(
                (Integer) placeBlockMap.get("timetableId"),
                placeBlockId, // timetablePlaceBlockId
                (Integer) placeBlockMap.get("placeCategoryId"),
                (String) placeBlockMap.get("placeName"),
                (Float) placeBlockMap.get("placeRating"),
                (String) placeBlockMap.get("placeAddress"),
                (String) placeBlockMap.get("placeLink"),
                (String) placeBlockMap.get("placeId"),
                (String) placeBlockMap.get("date"),
                placeBlockMap.containsKey("startTime") ? LocalTime.parse((String) placeBlockMap.get("startTime")) : null,
                placeBlockMap.containsKey("endTime") ? LocalTime.parse((String) placeBlockMap.get("endTime")) : null,
                (Double) placeBlockMap.get("xLocation"),
                (Double) placeBlockMap.get("yLocation")
            );
            
            request.setTimetablePlaceBlockVO(placeBlockVO);
            
            String userMessage = "장소 정보를 업데이트했습니다! ✏️";
            
            return ChatBotActionResponse.withAction(userMessage, "update", "timeTablePlaceBlock", request);
            
        } catch (Exception e) {
            log.error("장소 블록 업데이트 실패: {}", e.getMessage());
            return ChatBotActionResponse.simpleMessage("장소 업데이트에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 장소 블록 삭제
     */
    public ChatBotActionResponse deleteTimeTablePlaceBlock(Integer placeBlockId) {
        try {
            log.info("AI 요청: 장소 블록 삭제 - placeBlockId: {}", placeBlockId);
            
            // WTimeTablePlaceBlockRequest 객체 생성
            WTimeTablePlaceBlockRequest request = new WTimeTablePlaceBlockRequest();
            
            // 삭제 시에는 ID만 필요하므로 나머지는 기본값으로 설정
            TimetablePlaceBlockVO placeBlockVO = new TimetablePlaceBlockVO(
                0, // timetableId - primitive int이므로 0으로 설정
                placeBlockId, // timetablePlaceBlockId
                null, // placeCategoryId
                null, // placeName
                null, // placeRating
                null, // placeAddress
                null, // placeLink
                null, // placeId
                null, // date
                null, // startTime
                null, // endTime
                null, // xLocation
                null  // yLocation
            );
            
            request.setTimetablePlaceBlockVO(placeBlockVO);
            
            String userMessage = "장소를 일정에서 제거했습니다! 🗑️";
            
            return ChatBotActionResponse.withAction(userMessage, "delete", "timeTablePlaceBlock", request);
            
        } catch (Exception e) {
            log.error("장소 블록 삭제 실패: {}", e.getMessage());
            return ChatBotActionResponse.simpleMessage("장소 삭제에 실패했습니다: " + e.getMessage());
        }
    }
}