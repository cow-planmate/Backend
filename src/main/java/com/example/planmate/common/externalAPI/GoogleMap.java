package com.example.planmate.common.externalAPI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
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
        String url = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" + query + "&key=" + googleApiKey + "&language=ko";
        String response = restTemplate.getForObject(url, String.class);
        return new StringBuilder(response != null ? response : "");

    }

    public Pair<List<TourPlaceVO>, List<String>> getTourPlace(String locationText, List<String> preferredThemeNames) throws IOException {
        return getTourPlace(locationText, preferredThemeNames, null, null);
    }

    public Pair<List<TourPlaceVO>, List<String>> getTourPlace(String locationText, List<String> preferredThemeNames, Double lat, Double lng) throws IOException {
        List<TourPlaceVO> places = new ArrayList<>();
        // For recommendations, we SHOULD append the location name to the query
        Pair<JsonNode, List<String>> pair = searchGoogleOrWithJackson("관광지", preferredThemeNames, locationText, lat, lng, 50000, 4.0, 0, true);
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
        return getLodgingPlace(locationText, preferredThemeNames, null, null);
    }

    public Pair<List<LodgingPlaceVO>, List<String>> getLodgingPlace(String locationText, List<String> preferredThemeNames, Double lat, Double lng) throws IOException {
        List<LodgingPlaceVO> places = new ArrayList<>();
        // For recommendations, we SHOULD append the location name to the query
        Pair<JsonNode, List<String>> pair = searchGoogleOrWithJackson("숙소", preferredThemeNames, locationText, lat, lng, 50000, 4.0, 0, true);
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
        return getRestaurantPlace(locationText, preferredThemeNames, null, null);
    }

    public Pair<List<RestaurantPlaceVO>, List<String>> getRestaurantPlace(String locationText, List<String> preferredThemeNames, Double lat, Double lng) throws IOException {
        List<RestaurantPlaceVO> places = new ArrayList<>();
        // For recommendations, we SHOULD append the location name to the query
        Pair<JsonNode, List<String>> pair = searchGoogleOrWithJackson("식당", preferredThemeNames, locationText, lat, lng, 50000, 4.0, 0, true);
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

    public Pair<Double, Double> getCoordinates(String query) throws IOException {
        String url = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" + query + "&key=" + googleApiKey + "&language=ko";
        String response = restTemplate.getForObject(url, String.class);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response);
        JsonNode results = root.path("results");

        if (results.isArray() && results.size() > 0) {
            JsonNode location = results.get(0).path("geometry").path("location");
            double lat = location.path("lat").asDouble(0.0);
            double lng = location.path("lng").asDouble(0.0);
            return Pair.of(lat, lng);
        }
        return null;
    }

    public Pair<List<SearchPlaceVO>, List<String>> getSearchPlace(String query) throws IOException {
        return getSearchPlace(query, null, null, null);
    }

    public Pair<List<SearchPlaceVO>, List<String>> getSearchPlace(String query, String locationText) throws IOException {
        return getSearchPlace(query, locationText, null, null);
    }

    public Pair<List<SearchPlaceVO>, List<String>> getSearchPlace(String query, String locationText, Double lat, Double lng) throws IOException {
        List<SearchPlaceVO> places = new ArrayList<>();
        // For general search, we should NOT append the location name (to allow global search like "Seoul Station")
        // But we still apply locationBias via lat/lng if provided
        Pair<JsonNode, List<String>> pair = searchGoogleOrWithJackson(query, null, locationText, lat, lng, 50000, 0.0, 0, false);
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

    private Pair<JsonNode, List<String>> searchGoogleOrWithJackson(
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
        finalJson.set("results", merged);
        JsonNode root = mapper.readTree(finalJson.toString());
        JsonNode results = root.get("results");
        return Pair.of(results, nextPageTokens);
    }

    private void searchAndFillMap(
            String fullQuery,
            String theme,
            Map<String, JsonNode> placeMap,
            List<String> nextPageTokens,
            Double lat, Double lng,
            Integer radiusMeters,
            Double minRating,
            int minReviews
    ) throws IOException {
        String currentQuery = fullQuery;
        if (theme != null && !theme.isBlank()) {
            currentQuery += " " + theme;
        }

        StringBuilder urlBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/place/textsearch/json?");
        urlBuilder.append("query=").append(currentQuery);
        if (lat != null && lng != null) {
            urlBuilder.append("&location=").append(lat).append(",").append(lng);
            urlBuilder.append("&radius=").append(radiusMeters != null ? radiusMeters : 5000);
        }
        urlBuilder.append("&key=").append(googleApiKey);
        urlBuilder.append("&language=ko");

        String raw = restTemplate.getForObject(urlBuilder.toString(), String.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(raw);

        String token = root.path("next_page_token").asText(null);
        if (token != null) {
            nextPageTokens.add(token);
        }

        JsonNode results = root.path("results");
        double effectiveMinRating = (minRating != null) ? minRating : 4.0;

        if (results.isArray()) {
            for (JsonNode place : results) {
                double rating = place.path("rating").asDouble(0.0);
                int reviews = place.path("user_ratings_total").asInt(0);
                if (rating >= effectiveMinRating && (minReviews <= 0 || reviews >= minReviews)) {
                    String placeId = place.path("place_id").asText(null);
                    if (placeId != null && !placeId.isBlank()) {
                        placeMap.put(placeId, place);
                    }
                }
            }
        }
    }

    private Pair<JsonNode, List<String>> searchGoogleNextPagePlace(List<String> nextPageTokens, Double minRating) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<String> nextNextPageTokens = new ArrayList<>();
        Map<String, JsonNode> placeMap = new LinkedHashMap<>();

        for (String nextPageToken : nextPageTokens) {
            if (nextPageToken == null || nextPageToken.isBlank()) continue;

            String url = "https://maps.googleapis.com/maps/api/place/textsearch/json?pagetoken=" + nextPageToken + "&key=" + googleApiKey;

            String raw = restTemplate.getForObject(url, String.class);
            JsonNode root = mapper.readTree(raw);
            
            String nextToken = root.path("next_page_token").asText(null);
            if (nextToken != null) {
                nextNextPageTokens.add(nextToken);
            }
            JsonNode results = root.path("results");
            if (results != null && results.isArray()) {
                double effectiveMinRating = (minRating != null) ? minRating : 4.0;
                for (JsonNode place : results) {
                    double rating = place.path("rating").asDouble(0.0);
                    if (rating >= effectiveMinRating) {
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
