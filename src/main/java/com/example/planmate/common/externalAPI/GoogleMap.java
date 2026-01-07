package com.example.planmate.common.externalAPI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.example.planmate.common.valueObject.DepartureVO;
import com.example.planmate.common.valueObject.LodgingPlaceVO;
import com.example.planmate.common.valueObject.RestaurantPlaceVO;
import com.example.planmate.common.valueObject.SearchPlaceVO;
import com.example.planmate.common.valueObject.TourPlaceVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class GoogleMap {
    @Value("${api.google.key}")
    private String googleApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public StringBuilder searchGoogle(String query) throws IOException {
        // Updated to use Places API (New) for searchDeparture consistency
        String url = "https://places.googleapis.com/v1/places:searchText";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Goog-Api-Key", googleApiKey);
        headers.set("X-Goog-FieldMask", "places.id,places.displayName,places.formattedAddress");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("textQuery", query);
        body.put("languageCode", "ko");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        String response = restTemplate.postForObject(url, entity, String.class);

        return new StringBuilder(response != null ? response : "");
    }

    public Pair<List<TourPlaceVO>, List<String>> getTourPlace(String locationText, List<String> preferredThemeNames) throws IOException {
        List<TourPlaceVO> places = new ArrayList<>();
        Pair<JsonNode, List<String>> pair = searchGoogleOrWithJackson("관광지", preferredThemeNames, locationText, null, null, null, 0.0, 0);
        JsonNode results = pair.getFirst();
        List<String> nextPageTokens = pair.getSecond();

        if (results.isArray()) {
            for (JsonNode result : results) {
                String placeId = result.path("id").asText("");
                String url = "https://www.google.com/maps/place/?q=place_id:" + placeId;
                String name = result.path("displayName").path("text").asText("");
                String formatted_address = result.path("formattedAddress").asText("").replaceAll("…", "");
                float rating = (float) result.path("rating").asDouble(0.0);

                JsonNode location = result.path("location");
                double xLocation = location.path("longitude").asDouble(0.0);
                double yLocation = location.path("latitude").asDouble(0.0);
                String iconUrl = result.path("iconMaskBaseUri").asText("");

                TourPlaceVO place = new TourPlaceVO(placeId, 0, url, name, formatted_address, rating, xLocation, yLocation, iconUrl);
                places.add(place);
            }
        }
        return Pair.of(places, nextPageTokens);
    }

    public Pair<List<LodgingPlaceVO>, List<String>> getLodgingPlace(String locationText, List<String> preferredThemeNames) throws IOException {
        List<LodgingPlaceVO> places = new ArrayList<>();
        Pair<JsonNode, List<String>> pair = searchGoogleOrWithJackson("숙소", preferredThemeNames, locationText, null, null, null, 0.0, 0);
        JsonNode results = pair.getFirst();
        List<String> nextPageTokens = pair.getSecond();
        if (results != null && results.isArray()) {
            for (JsonNode result : results) {
                String placeId = result.path("id").asText("");
                String url = "https://www.google.com/maps/place/?q=place_id:" + placeId;
                String name = result.path("displayName").path("text").asText("");
                String formatted_address = result.path("formattedAddress").asText("").replaceAll("…", "");
                float rating = (float) result.path("rating").asDouble(0.0);

                JsonNode location = result.path("location");
                double xLocation = location.path("longitude").asDouble(0.0);
                double yLocation = location.path("latitude").asDouble(0.0);
                String iconUrl = result.path("iconMaskBaseUri").asText("");

                LodgingPlaceVO place = new LodgingPlaceVO(placeId, 1, url, name, formatted_address, rating, xLocation, yLocation, iconUrl);
                places.add(place);
            }
        }
        return Pair.of(places, nextPageTokens);
    }
    public Pair<List<RestaurantPlaceVO>, List<String>> getRestaurantPlace(String locationText, List<String> preferredThemeNames) throws IOException {
        List<RestaurantPlaceVO> places = new ArrayList<>();
        Pair<JsonNode, List<String>> pair = searchGoogleOrWithJackson("식당", preferredThemeNames, locationText, null, null, null, 0.0, 0);
        JsonNode results = pair.getFirst();
        List<String> nextPageTokens = pair.getSecond();
        if (results != null && results.isArray()) {
            for (JsonNode result : results) {
                String placeId = result.path("id").asText("");
                String url = "https://www.google.com/maps/place/?q=place_id:" + placeId;
                String name = result.path("displayName").path("text").asText("");
                String formatted_address = result.path("formattedAddress").asText("").replaceAll("…", "");
                float rating = (float) result.path("rating").asDouble(0.0);

                JsonNode location = result.path("location");
                double xLocation = location.path("longitude").asDouble(0.0);
                double yLocation = location.path("latitude").asDouble(0.0);
                String iconUrl = result.path("iconMaskBaseUri").asText("");

                RestaurantPlaceVO place = new RestaurantPlaceVO(placeId, 2, url, name, formatted_address, rating, xLocation, yLocation, iconUrl);
                places.add(place);
            }
        }
        return Pair.of(places, nextPageTokens);
    }

    public Pair<List<SearchPlaceVO>, List<String>> getSearchPlace(String query) throws IOException {
        List<SearchPlaceVO> places = new ArrayList<>();
        Pair<JsonNode, List<String>> pair = searchGoogleOrWithJackson(query, null, null, null, null, null, 0.0, 0);
        JsonNode results = pair.getFirst();
        List<String> nextPageTokens = pair.getSecond();
        if (results != null && results.isArray()) {
            for (JsonNode result : results) {
                String placeId = result.path("id").asText("");
                String url = "https://www.google.com/maps/place/?q=place_id:" + placeId;
                String name = result.path("displayName").path("text").asText("");
                String formatted_address = result.path("formattedAddress").asText("").replaceAll("…", "");
                float rating = (float) result.path("rating").asDouble(0.0);

                JsonNode location = result.path("location");
                double xLocation = location.path("longitude").asDouble(0.0);
                double yLocation = location.path("latitude").asDouble(0.0);
                String iconUrl = result.path("iconMaskBaseUri").asText("");

                places.add(new SearchPlaceVO(placeId, 4, url, name, formatted_address, rating, xLocation, yLocation, iconUrl));
            }
        }
        return Pair.of(places, nextPageTokens);
    }

    public List<DepartureVO> searchDeparture(String departureName) throws IOException {
        StringBuilder sb = searchGoogle(departureName);
        List<DepartureVO> departures = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(sb.toString());
        JsonNode results = root.get("places");

        if (results != null && results.isArray()) {
            for (JsonNode result : results) {
                String placeId = result.path("id").asText("");
                String url = "https://www.google.com/maps/place/?q=place_id:" + placeId;
                String name = result.path("displayName").path("text").asText("");
                String formatted_address = result.path("formattedAddress").asText("").replaceAll("…", "");
                departures.add(new DepartureVO(placeId, url, name, formatted_address));
            }
        }
        return departures;
    }

    public Pair<List<SearchPlaceVO>, List<String>> getNextPagePlace(List<String> nextPageTokens) throws IOException {
        List<SearchPlaceVO> places = new ArrayList<>();
        Pair<JsonNode, List<String>> pair = searchGoogleNextPagePlace(nextPageTokens, Double.valueOf(0));
        JsonNode results = pair.getFirst();
        List<String> nextNextPageTokens = pair.getSecond();
        if (results != null && results.isArray()) {
            for (JsonNode result : results) {
                String placeId = result.path("id").asText("");
                String url = "https://www.google.com/maps/place/?q=place_id:" + placeId;
                String name = result.path("displayName").path("text").asText("");
                String formatted_address = result.path("formattedAddress").asText("").replaceAll("…", "");
                float rating = (float) result.path("rating").asDouble(0.0);

                JsonNode location = result.path("location");
                double xLocation = location.path("longitude").asDouble(0.0);
                double yLocation = location.path("latitude").asDouble(0.0);
                String iconUrl = result.path("iconMaskBaseUri").asText("");

                places.add(new SearchPlaceVO(placeId, 4, url, name, formatted_address, rating, xLocation, yLocation, iconUrl));
            }
        }
        return Pair.of(places, nextNextPageTokens);
    }

    private Pair<JsonNode, List<String>> searchGoogleOrWithJackson(
            String query,
            List<String> preferredThemeNames,
            String locationText,
            Double lat,
            Double lng,
            Integer radiusMeters,
            Double minRating,
            int minReviews
    ) throws IOException {

        final String url = "https://places.googleapis.com/v1/places:searchText";
        ObjectMapper mapper = new ObjectMapper();
        // query + (선택) 위치 문자열을 합친다.
        String fullQuery = (locationText == null || locationText.isBlank())
                ? query
                : (query + " " + locationText);


        // id → place(JsonNode) 저장: LinkedHashMap으로 순서 보존
        Map<String, JsonNode> placeMap = new LinkedHashMap<>();

        // 테마 기반 검색 수행
        List<String> themes = (preferredThemeNames == null || preferredThemeNames.isEmpty())
                ? new ArrayList<>() : new ArrayList<>(preferredThemeNames);
        
        List<String> nextPageTokens = new ArrayList<>();

        // 1차 검색: 테마 기반
        for (String theme : themes) {
            searchAndFillMap(fullQuery, theme, placeMap, nextPageTokens, lat, lng, radiusMeters, minRating, minReviews);
        }

        // 2차 검색: 결과가 10개 미만일 경우에만 기본(Baseline) 검색 추가
        if (placeMap.size() < 10) {
            searchAndFillMap(fullQuery, "", placeMap, nextPageTokens, lat, lng, radiusMeters, minRating, minReviews);
        }

        // 최종 JSON 구성
        ObjectNode finalJson = mapper.createObjectNode();
        ArrayNode merged = mapper.createArrayNode();
        placeMap.values().forEach(merged::add);
        finalJson.set("places", merged);
        JsonNode root = mapper.readTree(finalJson.toString());
        JsonNode results = root.get("places");
        return Pair.of(results, nextPageTokens);
    }

    private void searchAndFillMap(
            String fullQuery,
            String theme,
            Map<String, JsonNode> placeMap,
            List<String> nextPageTokens,
            Double lat,
            Double lng,
            Integer radiusMeters,
            Double minRating,
            int minReviews
    ) throws IOException {
        final String url = "https://places.googleapis.com/v1/places:searchText";
        ObjectMapper mapper = new ObjectMapper();
        
        String currentQuery = fullQuery;
        if (theme != null && !theme.isBlank()) {
            currentQuery += " " + theme;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Goog-Api-Key", googleApiKey);
        headers.set("X-Goog-FieldMask", "places.id,places.displayName,places.formattedAddress,places.rating,places.location,places.iconMaskBaseUri,places.userRatingCount,nextPageToken");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("textQuery", currentQuery);
        body.put("languageCode", "ko");

        if (lat != null && lng != null) {
            Map<String, Object> locationBias = new LinkedHashMap<>();
            Map<String, Object> circle = new LinkedHashMap<>();
            Map<String, Object> center = new LinkedHashMap<>();
            center.put("latitude", lat);
            center.put("longitude", lng);
            circle.put("center", center);
            circle.put("radius", radiusMeters != null ? radiusMeters.doubleValue() : 5000.0);
            locationBias.put("circle", circle);
            body.put("locationBias", locationBias);
        }

        // 별점 필터는 4.0 유지
        double currentMinRating = (minRating != null && minRating > 0) ? minRating : 4.0;

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        String raw = restTemplate.postForObject(url, entity, String.class);
        JsonNode root = mapper.readTree(raw);
        
        String token = root.path("nextPageToken").asText(null);
        if (token != null) nextPageTokens.add(token);

        JsonNode results = root.path("places");
        if (results.isArray()) {
            for (JsonNode place : results) {
                double rating = place.path("rating").asDouble(0.0);
                int reviews = place.path("userRatingCount").asInt(0);
                if (rating >= currentMinRating && (minReviews <= 0 || reviews >= minReviews)) {
                    String placeId = place.path("id").asText(null);
                    if (placeId != null && !placeId.isBlank()) {
                        placeMap.put(placeId, place);
                    }
                }
            }
        }
    }

    private Pair<JsonNode, List<String>> searchGoogleNextPagePlace(List<String> nextPageTokens, Double minRating) throws IOException {

        final String url = "https://places.googleapis.com/v1/places:searchText";
        ObjectMapper mapper = new ObjectMapper();
        List<String> nextNextPageTokens = new ArrayList<>();
        Map<String, JsonNode> placeMap = new LinkedHashMap<>();

        for (String nextPageToken : nextPageTokens) {
            if (nextPageToken == null || nextPageToken.isBlank()) continue;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Goog-Api-Key", googleApiKey);
            headers.set("X-Goog-FieldMask", "places.id,places.displayName,places.formattedAddress,places.rating,places.location,places.iconMaskBaseUri,nextPageToken");

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("pageToken", nextPageToken);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            String raw = restTemplate.postForObject(url, entity, String.class);
            JsonNode root = mapper.readTree(raw);
            
            nextNextPageTokens.add(root.path("nextPageToken").asText(null));
            JsonNode results = root.path("places");
            if (results != null && results.isArray()) {
                for (JsonNode place : results) {
                    double rating = place.path("rating").asDouble(0.0);
                    double currentMinRating = (minRating != null && minRating > 0) ? minRating : 4.0;
                    if (rating >= currentMinRating) {
                        String placeId = place.path("id").asText(null);
                        if (placeId != null && !placeId.isBlank()) {
                            placeMap.put(placeId, place); // 중복이면 덮어쓴다.
                        }
                    }
                }
            }
        }
        // 최종 JSON 구성
        ObjectNode finalJson = mapper.createObjectNode();
        ArrayNode merged = mapper.createArrayNode();
        placeMap.values().forEach(merged::add);
        finalJson.set("places", merged);
        JsonNode root = mapper.readTree(finalJson.toString());
        JsonNode results = root.get("places");
        return Pair.of(results, nextNextPageTokens);
    }


}
