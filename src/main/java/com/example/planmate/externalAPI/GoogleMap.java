package com.example.planmate.externalAPI;

import com.example.planmate.valueObject.DepartureVO;
import com.example.planmate.valueObject.LodgingPlaceVO;
import com.example.planmate.valueObject.RestaurantPlaceVO;
import com.example.planmate.valueObject.TourPlaceVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
    public List<TourPlaceVO> getTourPlace(String query) throws IOException {
        StringBuilder sb = searchGoogle(query);
        List<TourPlaceVO> places = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(sb.toString());
        JsonNode results = root.get("results");

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

                TourPlaceVO place = new TourPlaceVO(placeId, url, name, formatted_address, rating, xLocation, yLocation, iconUrl);
                places.add(place);
            }
        }

        return places;
    }

    public List<LodgingPlaceVO> getLodgingPlace(String query) throws IOException {
        StringBuilder sb = searchGoogle(query);
        List<LodgingPlaceVO> places = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(sb.toString());
        JsonNode results = root.get("results");

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

                LodgingPlaceVO place = new LodgingPlaceVO(placeId, url, name, formatted_address, rating, xLocation, yLocation, iconUrl);
                places.add(place);
            }
        }

        return places;
    }
    public List<RestaurantPlaceVO> getRestaurantPlace(String query) throws IOException {
        StringBuilder sb = searchGoogle(query);
        List<RestaurantPlaceVO> places = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(sb.toString());
        JsonNode results = root.get("results");

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

                RestaurantPlaceVO place = new RestaurantPlaceVO(placeId, url, name, formatted_address, rating, xLocation, yLocation, iconUrl);
                places.add(place);
            }
        }

        return places;
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

    public String getTransitTime(String origin, String destination) throws IOException {
//        String apiKey = "YOUR_API_KEY";
//        String urlStr = "https://maps.googleapis.com/maps/api/directions/json"
//                + "?origin=" + URLEncoder.encode(origin, "UTF-8")
//                + "&destination=" + URLEncoder.encode(destination, "UTF-8")
//                + "&mode=transit"
//                + "&language=ko"
//                + "&key=" + apiKey;
//
//        URL url = new URL(urlStr);
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//        conn.setRequestMethod("GET");
//
//        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//        StringBuilder sb = new StringBuilder();
//        String inputLine;
//        while ((inputLine = in.readLine()) != null) {
//            sb.append(inputLine);
//        }
//        in.close();
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        JsonNode root = objectMapper.readTree(sb.toString());
//        JsonNode routes = root.get("routes");
//
//        if (routes != null && routes.isArray() && routes.size() > 0) {
//            JsonNode leg = routes.get(0).path("legs").get(0);
//
//            String startAddress = leg.path("start_address").asText("");
//            String endAddress = leg.path("end_address").asText("");
//            String distanceText = leg.path("distance").path("text").asText("");  // 예: "13.1km"
//            String durationText = leg.path("duration").path("text").asText("");  // 예: "28분"
//
//            double startLat = leg.path("start_location").path("lat").asDouble(0.0);
//            double startLng = leg.path("start_location").path("lng").asDouble(0.0);
//            double endLat = leg.path("end_location").path("lat").asDouble(0.0);
//            double endLng = leg.path("end_location").path("lng").asDouble(0.0);
//
//            // 너가 사용할 VO 클래스가 있다면 아래와 같이 사용
//            DirectionInfoVO direction = new DirectionInfoVO(
//                    startAddress,
//                    endAddress,
//                    distanceText,
//                    durationText,
//                    startLat,
//                    startLng,
//                    endLat,
//                    endLng
//            );
//
//            // 예: List<DirectionInfoVO> directions = new ArrayList<>();
//            directions.add(direction);
        return null;
    }
}
