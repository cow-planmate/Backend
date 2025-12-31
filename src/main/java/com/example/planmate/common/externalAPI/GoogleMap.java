package com.example.planmate.common.externalAPI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

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
    public StringBuilder searchGoogle(String query) throws IOException {
        String urlStr = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" +
                java.net.URLEncoder.encode(query, "UTF-8") + "&language=ko" + "&key=" + googleApiKey;

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null)
            response.append(inputLine);
        in.close();
        return response;
    }

    public Pair<List<TourPlaceVO>, List<String>> getTourPlace(String locationText, List<String> preferredThemeNames) throws IOException {
        List<TourPlaceVO> places = new ArrayList<>();
        Pair<JsonNode, List<String>> pair = searchGoogleOrWithJackson("관광지", preferredThemeNames, locationText, null, null, null, 0.0, 0);
        JsonNode results = pair.getFirst();
        List<String> nextPageTokens = pair.getSecond();

        if (results.isArray()) {
            for (JsonNode result : results) {
                String placeId = result.path("place_id").asText("");
                String url = "https://www.google.com/maps/place/?q=place_id:" + placeId;
                String name = result.path("name").asText("");
                String formatted_address = result.path("formatted_address").asText("").replaceAll("…", "");
                float rating = (float) result.path("rating").asDouble(0.0);

                JsonNode location = result.path("geometry").path("location");
                double xLocation = location.path("lng").asDouble(0.0);
                double yLocation = location.path("lat").asDouble(0.0);
                String iconUrl = result.path("icon").asText("");

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
                String placeId = result.path("place_id").asText("");
                String url = "https://www.google.com/maps/place/?q=place_id:" + placeId;
                String name = result.path("name").asText("");
                String formatted_address = result.path("formatted_address").asText("").replaceAll("…", "");
                float rating = (float) result.path("rating").asDouble(0.0);

                JsonNode location = result.path("geometry").path("location");
                double xLocation = location.path("lng").asDouble(0.0);
                double yLocation = location.path("lat").asDouble(0.0);
                String iconUrl = result.path("icon").asText("");

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
                String placeId = result.path("place_id").asText("");
                String url = "https://www.google.com/maps/place/?q=place_id:" + placeId;
                String name = result.path("name").asText("");
                String formatted_address = result.path("formatted_address").asText("").replaceAll("…", "");
                float rating = (float) result.path("rating").asDouble(0.0);

                JsonNode location = result.path("geometry").path("location");
                double xLocation = location.path("lng").asDouble(0.0);
                double yLocation = location.path("lat").asDouble(0.0);
                String iconUrl = result.path("icon").asText("");

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
                String placeId = result.path("place_id").asText("");
                String url = "https://www.google.com/maps/place/?q=place_id:" + placeId;
                String name = result.path("name").asText("");
                String formatted_address = result.path("formatted_address").asText("").replaceAll("…", "");
                float rating = (float) result.path("rating").asDouble(0.0);

                JsonNode location = result.path("geometry").path("location");
                double xLocation = location.path("lng").asDouble(0.0);
                double yLocation = location.path("lat").asDouble(0.0);
                String iconUrl = result.path("icon").asText("");

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
        JsonNode results = root.get("results");

        if (results != null && results.isArray()) {
            for (JsonNode result : results) {
                String placeId = result.path("place_id").asText("");
                String url = "https://www.google.com/maps/place/?q=place_id:" + placeId;
                String name = result.path("name").asText("");
                String formatted_address = result.path("formatted_address").asText("").replaceAll("…", "");
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
                String placeId = result.path("place_id").asText("");
                String url = "https://www.google.com/maps/place/?q=place_id:" + placeId;
                String name = result.path("name").asText("");
                String formatted_address = result.path("formatted_address").asText("").replaceAll("…", "");
                float rating = (float) result.path("rating").asDouble(0.0);

                JsonNode location = result.path("geometry").path("location");
                double xLocation = location.path("lng").asDouble(0.0);
                double yLocation = location.path("lat").asDouble(0.0);
                String iconUrl = result.path("icon").asText("");

                places.add(new SearchPlaceVO(placeId, 4, url, name, formatted_address, rating, xLocation, yLocation, iconUrl));
            }
        }
        return Pair.of(places, nextNextPageTokens);
    }

    private String httpGet(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder raw = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) raw.append(line);
            return raw.toString();
        }
    }

    // ===== URL 쿼리 파라미터 안전 인코딩 =====
    private String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    /**
     * OR 검색 + 별점/리뷰수 필터 + (선택) 위치 문자열/좌표 반경.
     *
     * @param query               기본 검색어 (예: "맛집", "카페")
     * @param preferredThemeNames 선호 테마 목록 (각 테마별로 별도 호출 → OR 효과)
     * @param locationText        위치 키워드(예: "강남", "부산 해운대"). null 가능.
     * @param lat                 중심 위도. null 가능.
     * @param lng                 중심 경도. null 가능.
     * @param radiusMeters        반경(m). lat/lng 주면 함께 사용. null 가능.
     * @param minRating           최소 별점(기본 4.0 추천)
     * @param minReviews          최소 리뷰 수(노이즈 방지용, 예: 50). 0이면 미적용.
     * @return                    합쳐진 JSON(results 배열 포함)을 StringBuilder로 반환
     */
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

        final String base = "https://maps.googleapis.com/maps/api/place/textsearch/json";
        ObjectMapper mapper = new ObjectMapper();
        // query + (선택) 위치 문자열을 합친다.
        String fullQuery = (locationText == null || locationText.isBlank())
                ? query
                : (query + " " + locationText);


        // place_id → place(JsonNode) 저장: LinkedHashMap으로 순서 보존
        Map<String, JsonNode> placeMap = new LinkedHashMap<>();

        // 테마가 비었을 경우를 대비해 1회 호출(키워드 없이)도 가능하게 처리
        List<String> themes = (preferredThemeNames == null || preferredThemeNames.isEmpty())
                ? List.of("") : preferredThemeNames;
        List<String> nextPageTokens = new ArrayList<>();

        for (String theme : themes) {
            String currentQuery = fullQuery;
            if (theme != null && !theme.isBlank()) {
                currentQuery += " " + theme;
            }

            StringBuilder url = new StringBuilder(base)
                    .append("?query=").append(enc(currentQuery))
                    .append("&language=ko")
                    .append("&key=").append(enc(googleApiKey));

            // 좌표 기반 검색 옵션
            if (lat != null && lng != null) {
                url.append("&location=").append(lat).append(",").append(lng);
                if (radiusMeters != null && radiusMeters > 0) {
                    url.append("&radius=").append(radiusMeters);
                }
            }
            minRating = 4.0;

            String raw = httpGet(url.toString());
            JsonNode root = mapper.readTree(raw);
            nextPageTokens.add(root.path("next_page_token").asText(null));
            JsonNode results = root.path("results");
            if (results.isArray()) {
                for (JsonNode place : results) {
                    double rating = place.path("rating").asDouble(0.0);
                    int reviews = place.path("user_ratings_total").asInt(0);
                    if (rating >= minRating && (minReviews <= 0 || reviews >= minReviews)) {
                        String placeId = place.path("place_id").asText(null);
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
        finalJson.set("results", merged);
        JsonNode root = mapper.readTree(finalJson.toString());
        JsonNode results = root.get("results");
        return Pair.of(results, nextPageTokens);
    }

    private Pair<JsonNode, List<String>> searchGoogleNextPagePlace(List<String> nextPageTokens, Double minRating) throws IOException {

        final String base = "https://maps.googleapis.com/maps/api/place/textsearch/json";
        ObjectMapper mapper = new ObjectMapper();
        List<String> nextNextPageTokens = new ArrayList<>();
        Map<String, JsonNode> placeMap = new LinkedHashMap<>();

        for (String nextPageToken : nextPageTokens) {
            StringBuilder url = new StringBuilder(base)
                    .append("?language=ko")
                    .append("&key=").append(enc(googleApiKey))
                    .append("&pagetoken=").append(enc(nextPageToken));

            String raw = httpGet(url.toString());
            JsonNode root = mapper.readTree(raw);
            nextNextPageTokens.add(root.path("next_page_token").asText(null));
            JsonNode results = root.path("results");
            if (results.isArray()) {
                for (JsonNode place : results) {
                    double rating = place.path("rating").asDouble(0.0);
                    if (rating >= minRating) {
                        String placeId = place.path("place_id").asText(null);
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
        finalJson.set("results", merged);
        JsonNode root = mapper.readTree(finalJson.toString());
        JsonNode results = root.get("results");
        return Pair.of(results, nextNextPageTokens);
    }


}
