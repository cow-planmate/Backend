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
import com.example.planmate.common.valueObject.NextPageTokenVO;
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

    public Pair<List<TourPlaceVO>, List<NextPageTokenVO>> getTourPlace(String locationText, List<String> preferredThemeNames) throws IOException {
        return getTourPlace(locationText, preferredThemeNames, null, null);
    }

    public Pair<List<TourPlaceVO>, List<NextPageTokenVO>> getTourPlace(String locationText, List<String> preferredThemeNames, Double lat, Double lng) throws IOException {
        List<TourPlaceVO> places = new ArrayList<>();
        // For recommendations, we SHOULD append the location name to the query
        Pair<JsonNode, List<NextPageTokenVO>> pair = searchGoogleOrWithJackson("관광지", preferredThemeNames, locationText, lat, lng, 50000, 0.0, 0, true);
        JsonNode results = pair.getFirst();
        List<NextPageTokenVO> nextPageTokens = pair.getSecond();

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
                if (!iconUrl.isEmpty()) {
                    iconUrl += ".svg";
                }

                TourPlaceVO place = new TourPlaceVO(placeId, 0, url, name, formatted_address, rating, xLocation, yLocation, iconUrl);
                places.add(place);
            }
        }
        return Pair.of(places, nextPageTokens);
    }

    public Pair<List<LodgingPlaceVO>, List<NextPageTokenVO>> getLodgingPlace(String locationText, List<String> preferredThemeNames) throws IOException {
        return getLodgingPlace(locationText, preferredThemeNames, null, null);
    }

    public Pair<List<LodgingPlaceVO>, List<NextPageTokenVO>> getLodgingPlace(String locationText, List<String> preferredThemeNames, Double lat, Double lng) throws IOException {
        List<LodgingPlaceVO> places = new ArrayList<>();
        // For recommendations, we SHOULD append the location name to the query
        Pair<JsonNode, List<NextPageTokenVO>> pair = searchGoogleOrWithJackson("숙소", preferredThemeNames, locationText, lat, lng, 50000, 0.0, 0, true);
        JsonNode results = pair.getFirst();
        List<NextPageTokenVO> nextPageTokens = pair.getSecond();
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
                if (!iconUrl.isEmpty()) {
                    iconUrl += ".svg";
                }

                LodgingPlaceVO place = new LodgingPlaceVO(placeId, 1, url, name, formatted_address, rating, xLocation, yLocation, iconUrl);
                places.add(place);
            }
        }
        return Pair.of(places, nextPageTokens);
    }

    public Pair<List<RestaurantPlaceVO>, List<NextPageTokenVO>> getRestaurantPlace(String locationText, List<String> preferredThemeNames) throws IOException {
        return getRestaurantPlace(locationText, preferredThemeNames, null, null);
    }

    public Pair<List<RestaurantPlaceVO>, List<NextPageTokenVO>> getRestaurantPlace(String locationText, List<String> preferredThemeNames, Double lat, Double lng) throws IOException {
        List<RestaurantPlaceVO> places = new ArrayList<>();
        // For recommendations, we SHOULD append the location name to the query
        Pair<JsonNode, List<NextPageTokenVO>> pair = searchGoogleOrWithJackson("식당", preferredThemeNames, locationText, lat, lng, 50000, 0.0, 0, true);
        JsonNode results = pair.getFirst();
        List<NextPageTokenVO> nextPageTokens = pair.getSecond();
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
                if (!iconUrl.isEmpty()) {
                    iconUrl += ".svg";
                }

                RestaurantPlaceVO place = new RestaurantPlaceVO(placeId, 2, url, name, formatted_address, rating, xLocation, yLocation, iconUrl);
                places.add(place);
            }
        }
        return Pair.of(places, nextPageTokens);
    }

    public Pair<Double, Double> getCoordinates(String query) throws IOException {
        String url = "https://places.googleapis.com/v1/places:searchText";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Goog-Api-Key", googleApiKey);
        headers.set("X-Goog-FieldMask", "places.location");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("textQuery", query);
        body.put("languageCode", "ko");
        body.put("maxResultCount", 1);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        String response = restTemplate.postForObject(url, entity, String.class);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response);
        JsonNode places = root.path("places");

        if (places.isArray() && places.size() > 0) {
            JsonNode location = places.get(0).path("location");
            double lat = location.path("latitude").asDouble(0.0);
            double lng = location.path("longitude").asDouble(0.0);
            return Pair.of(lat, lng);
        }
        return null;
    }

    public Pair<List<SearchPlaceVO>, List<NextPageTokenVO>> getSearchPlace(String query) throws IOException {
        return getSearchPlace(query, null, null, null);
    }

    public Pair<List<SearchPlaceVO>, List<NextPageTokenVO>> getSearchPlace(String query, String locationText) throws IOException {
        return getSearchPlace(query, locationText, null, null);
    }

    public Pair<List<SearchPlaceVO>, List<NextPageTokenVO>> getSearchPlace(String query, String locationText, Double lat, Double lng) throws IOException {
        List<SearchPlaceVO> places = new ArrayList<>();
        // For general search, we should NOT append the location name (to allow global search like "Seoul Station")
        // But we still apply locationBias via lat/lng if provided
        Pair<JsonNode, List<NextPageTokenVO>> pair = searchGoogleOrWithJackson(query, null, locationText, lat, lng, 50000, 0.0, 0, false);
        JsonNode results = pair.getFirst();
        List<NextPageTokenVO> nextPageTokens = pair.getSecond();
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
                if (!iconUrl.isEmpty()) {
                    iconUrl += ".svg";
                }

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

    public Pair<List<SearchPlaceVO>, List<NextPageTokenVO>> getNextPagePlace(List<NextPageTokenVO> nextPageTokens) throws IOException {
        List<SearchPlaceVO> places = new ArrayList<>();
        Pair<JsonNode, List<NextPageTokenVO>> pair = searchGoogleNextPagePlace(nextPageTokens, Double.valueOf(0));
        JsonNode results = pair.getFirst();
        List<NextPageTokenVO> nextNextPageTokens = pair.getSecond();
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
                if (!iconUrl.isEmpty()) {
                    iconUrl += ".svg";
                }

                places.add(new SearchPlaceVO(placeId, 4, url, name, formatted_address, rating, xLocation, yLocation, iconUrl));
            }
        }
        return Pair.of(places, nextNextPageTokens);
    }

    private Pair<JsonNode, List<NextPageTokenVO>> searchGoogleOrWithJackson(
            String query,
            List<String> preferredThemeNames,
            String locationText,
            Double lat,
            Double lng,
            Integer radiusMeters,
            Double minRating,
            int minReviews,
            boolean appendLocation
    ) throws IOException {

        final String url = "https://places.googleapis.com/v1/places:searchText";
        ObjectMapper mapper = new ObjectMapper();
        
        // Use flag to decide whether to force location text into the query string
        String fullQuery = (appendLocation && locationText != null && !locationText.isBlank())
                ? (query + " " + locationText)
                : query;


        // id → place(JsonNode) 저장: LinkedHashMap으로 순서 보존
        Map<String, JsonNode> placeMap = new LinkedHashMap<>();

        // 테마 기반 검색 수행
        List<String> themes = (preferredThemeNames == null || preferredThemeNames.isEmpty())
                ? new ArrayList<>() : new ArrayList<>(preferredThemeNames);
        
        List<NextPageTokenVO> nextPageTokens = new ArrayList<>();

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
            List<NextPageTokenVO> nextPageTokens,
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
        if (token != null) {
            // 위치 정보를 쿼리 문자열 뒤에 구분자와 함께 인코딩 (query|lat|lng|radius)
            String packedQuery = currentQuery;
            if (lat != null && lng != null) {
                packedQuery += "|" + lat + "|" + lng + "|" + (radiusMeters != null ? radiusMeters : 5000);
            }
            nextPageTokens.add(new NextPageTokenVO(token, packedQuery));
        }

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

    private Pair<JsonNode, List<NextPageTokenVO>> searchGoogleNextPagePlace(List<NextPageTokenVO> nextPageTokens, Double minRating) throws IOException {

        final String url = "https://places.googleapis.com/v1/places:searchText";
        ObjectMapper mapper = new ObjectMapper();
        List<NextPageTokenVO> nextNextPageTokens = new ArrayList<>();
        Map<String, JsonNode> placeMap = new LinkedHashMap<>();

        for (NextPageTokenVO tokenInfo : nextPageTokens) {
            String nextPageToken = tokenInfo.getToken();
            String packedQuery = tokenInfo.getQuery();
            if (nextPageToken == null || nextPageToken.isBlank()) continue;

            // packedQuery 파싱 (query|lat|lng|radius)
            String[] parts = packedQuery.split("\\|");
            String originalQuery = parts[0];

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Goog-Api-Key", googleApiKey);
            headers.set("X-Goog-FieldMask", "places.id,places.displayName,places.formattedAddress,places.rating,places.location,places.iconMaskBaseUri,nextPageToken");

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("textQuery", originalQuery);
            body.put("languageCode", "ko");
            body.put("pageToken", nextPageToken);

            if (parts.length >= 4) {
                try {
                    double lat = Double.parseDouble(parts[1]);
                    double lng = Double.parseDouble(parts[2]);
                    double radius = Double.parseDouble(parts[3]);

                    Map<String, Object> locationBias = new LinkedHashMap<>();
                    Map<String, Object> circle = new LinkedHashMap<>();
                    Map<String, Object> center = new LinkedHashMap<>();
                    center.put("latitude", lat);
                    center.put("longitude", lng);
                    circle.put("center", center);
                    circle.put("radius", radius);
                    locationBias.put("circle", circle);
                    body.put("locationBias", locationBias);
                } catch (NumberFormatException e) {
                    // 파싱 실패 시 무시하고 진행
                }
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            String raw = restTemplate.postForObject(url, entity, String.class);
            JsonNode root = mapper.readTree(raw);
            
            String nextToken = root.path("nextPageToken").asText(null);
            if (nextToken != null) {
                nextNextPageTokens.add(new NextPageTokenVO(nextToken, packedQuery));
            }
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
