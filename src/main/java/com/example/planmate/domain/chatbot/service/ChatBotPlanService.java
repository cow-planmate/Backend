package com.example.planmate.domain.chatbot.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import com.example.planmate.common.externalAPI.GoogleMap;
import com.example.planmate.common.externalAPI.GooglePlaceImageWorker;
import com.example.planmate.common.valueObject.PlaceVO;
import com.example.planmate.domain.chatbot.dto.ChatBotActionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sharedsync.dto.PlanDto;
import sharedsync.dto.TimeTableDto;
import sharedsync.dto.TimeTablePlaceBlockDto;
import sharedsync.wsdto.WPlanRequest;
import sharedsync.wsdto.WTimeTablePlaceBlockRequest;
import sharedsync.wsdto.WTimeTableRequest;

/**
 * AI 챗봇이 호출할 수 있는 여행 계획 관련 함수들을 정의
 * 사용자에게는 친근한 메시지, 시스템에게는 구조화된 액션 반환
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatBotPlanService {
    private final GooglePlaceImageWorker googlePlaceImageWorker;
    private final GoogleMap googleMap;
    private final ObjectMapper objectMapper;

    /**
     * 전체 계획 정보 업데이트 (JSON 형태로 받은 모든 필드를 처리)
     */
    public ChatBotActionResponse updateFullPlan(UUID planId, String planJson) {
        try {
            log.info("AI 요청: 전체 계획 업데이트 - planId: {}, planData: {}", planId, planJson);

            @SuppressWarnings("unchecked")
            Map<String, Object> planMap = objectMapper.readValue(planJson, Map.class);

            // PlanDto 생성 (planId 포함)
            planMap.put("planId", planId);
            PlanDto planDto = objectMapper.convertValue(planMap, PlanDto.class);

            // WPlanRequest 객체 생성 및 필드 설정
            WPlanRequest request = new WPlanRequest();
            request.setPlanDtos(List.of(planDto));

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
                    Object childCount = planMap.get("childCount");
                    if (childCount != null) {
                        messageBuilder.append(", 아이 ").append(childCount).append("명");
                    }
                }
                messageBuilder.append("\n");
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
    public ChatBotActionResponse createTimeTable(UUID planId, String timeTableJson) {
        try {
            log.info("AI 요청: 타임테이블 생성 - planId: {}, timeTableData: {}", planId, timeTableJson);

            @SuppressWarnings("unchecked")
            Map<String, Object> timeTableMap = objectMapper.readValue(timeTableJson, Map.class);

            // 필드명 변환 (startTime -> timeTableStartTime, endTime -> timeTableEndTime)
            if (timeTableMap.containsKey("startTime")) {
                timeTableMap.put("timeTableStartTime", timeTableMap.get("startTime"));
            } else if (!timeTableMap.containsKey("timeTableStartTime")) {
                timeTableMap.put("timeTableStartTime", "09:00:00");
            }
            if (timeTableMap.containsKey("endTime")) {
                timeTableMap.put("timeTableEndTime", timeTableMap.get("endTime"));
            } else if (!timeTableMap.containsKey("timeTableEndTime")) {
                timeTableMap.put("timeTableEndTime", "20:00:00");
            }
            timeTableMap.put("planId", planId);

            TimeTableDto timeTableDto = objectMapper.convertValue(timeTableMap, TimeTableDto.class);

            // WTimeTableRequest 객체 생성
            WTimeTableRequest request = new WTimeTableRequest();
            request.setTimeTableDtos(List.of(timeTableDto));

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

            @SuppressWarnings("unchecked")
            Map<String, Object> timeTableMap = objectMapper.readValue(timeTableJson, Map.class);

            // 필드명 변환
            timeTableMap.put("timeTableId", timeTableId);
            if (timeTableMap.containsKey("startTime")) {
                timeTableMap.put("timeTableStartTime", timeTableMap.get("startTime"));
            }
            if (timeTableMap.containsKey("endTime")) {
                timeTableMap.put("timeTableEndTime", timeTableMap.get("endTime"));
            }

            TimeTableDto timeTableDto = objectMapper.convertValue(timeTableMap, TimeTableDto.class);

            // WTimeTableRequest 객체 생성
            WTimeTableRequest request = new WTimeTableRequest();
            request.setTimeTableDtos(List.of(timeTableDto));

            String userMessage = "타임테이블 정보를 수정했습니다. ✏️";

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

            // WTimeTableRequest 객체 생성
            WTimeTableRequest request = new WTimeTableRequest();
            TimeTableDto timeTableDto = new TimeTableDto();

            // Use reflection to set timeTableId since there's no setter
            setField(timeTableDto, "timeTableId", timeTableId);

            request.setTimeTableDtos(List.of(timeTableDto));

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

            @SuppressWarnings("unchecked")
            Map<String, Object> placeBlockMap = objectMapper.readValue(placeBlockJson, Map.class);

            // timeTableId 설정
            placeBlockMap.put("timeTableId", timeTableId);

            // DTO 생성
            TimeTablePlaceBlockDto placeBlockDto = objectMapper.convertValue(placeBlockMap,
                    TimeTablePlaceBlockDto.class);

            String placeId = (String) placeBlockMap.get("placeId");
            String photoReference = (String) placeBlockMap.get("photoReference");
            try {
                if (placeId != null && !placeId.isEmpty()) {
                    googlePlaceImageWorker.fetchSinglePlaceDetailsAsync(placeId, photoReference);
                }
            } catch (Exception e) {
                log.warn("장소 이미지 가져오기 실패 (계속 진행): {}", e.getMessage());
            }

            // WTimeTablePlaceBlockRequest 객체 생성
            WTimeTablePlaceBlockRequest request = new WTimeTablePlaceBlockRequest();
            request.setTimeTablePlaceBlockDtos(List.of(placeBlockDto));

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

            @SuppressWarnings("unchecked")
            Map<String, Object> placeBlockMap = objectMapper.readValue(placeBlockJson, Map.class);

            // blockId 설정
            placeBlockMap.put("blockId", placeBlockId);

            // AI가 준 값 가져오기
            String placeName = (String) placeBlockMap.get("placeName");

            // placeName이 있고 placeId/좌표가 없거나 기본값일 때 Google API로 검색
            if (placeName != null && (!placeBlockMap.containsKey("placeId") || placeBlockMap.get("placeId") == null)) {
                try {
                    log.info("Google Places API로 장소 검색: {}", placeName);
                    Pair<List<PlaceVO>, List<String>> searchResult = googleMap.getSearchPlace(placeName);
                    List<PlaceVO> places = searchResult.getFirst();

                    if (places != null && !places.isEmpty()) {
                        PlaceVO foundPlace = places.get(0);

                        placeBlockMap.put("placeId", foundPlace.getPlaceId());
                        placeBlockMap.put("placeAddress", foundPlace.getFormatted_address());
                        placeBlockMap.put("placeLink", foundPlace.getUrl());
                        placeBlockMap.put("placeRating", foundPlace.getRating());
                        placeBlockMap.put("xLocation", foundPlace.getXLocation());
                        placeBlockMap.put("yLocation", foundPlace.getYLocation());
                        placeBlockMap.put("photoReference", foundPlace.getPhotoReference());

                        // 구글 검색 성공 시 비동기 이미지 업데이트 트리거
                        googlePlaceImageWorker.fetchSinglePlaceDetailsAsync(foundPlace.getPlaceId(),
                                foundPlace.getPhotoReference());

                        log.info("Google API 검색 성공 - placeId: {}", foundPlace.getPlaceId());
                    }
                } catch (IOException e) {
                    log.error("Google Places API 검색 실패: {}", e.getMessage());
                }
            }

            // DTO 생성
            TimeTablePlaceBlockDto placeBlockDto = objectMapper.convertValue(placeBlockMap,
                    TimeTablePlaceBlockDto.class);

            // WTimeTablePlaceBlockRequest 객체 생성
            WTimeTablePlaceBlockRequest request = new WTimeTablePlaceBlockRequest();
            request.setTimeTablePlaceBlockDtos(List.of(placeBlockDto));

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

            TimeTablePlaceBlockDto placeBlockDto = new TimeTablePlaceBlockDto();
            setField(placeBlockDto, "blockId", placeBlockId);

            request.setTimeTablePlaceBlockDtos(List.of(placeBlockDto));

            String userMessage = "장소를 일정에서 제거했습니다! 🗑️";

            return ChatBotActionResponse.withAction(userMessage, "delete", "timeTablePlaceBlock", request);

        } catch (Exception e) {
            log.error("장소 블록 삭제 실패: {}", e.getMessage());
            return ChatBotActionResponse.simpleMessage("장소 삭제에 실패했습니다: " + e.getMessage());
        }
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            log.error("Error setting field {} on {}: {}", fieldName, target.getClass().getSimpleName(), e.getMessage());
        }
    }

    private Float getFloatValue(Object rawValue) {
        if (rawValue == null)
            return null;
        if (rawValue instanceof Number)
            return ((Number) rawValue).floatValue();
        if (rawValue instanceof String) {
            try {
                return Float.parseFloat((String) rawValue);
            } catch (Exception e) {
            }
        }
        return null;
    }

    private Double getDoubleValue(Object rawValue) {
        if (rawValue == null)
            return null;
        if (rawValue instanceof Number)
            return ((Number) rawValue).doubleValue();
        if (rawValue instanceof String) {
            try {
                return Double.parseDouble((String) rawValue);
            } catch (Exception e) {
            }
        }
        return null;
    }
}
