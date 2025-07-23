package com.example.planmate.dto;

import com.example.planmate.valueObject.LodgingPlaceVO;
import com.example.planmate.valueObject.PlaceVO;
import com.example.planmate.valueObject.RestaurantPlaceVO;
import com.example.planmate.valueObject.TourPlaceVO;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class PlaceResponse extends CommonResponse{
    private int placeCategoryId;
    private List<PlaceVO> places;

    public PlaceResponse() {
        places = new ArrayList<>();
    }
    public void addLodgingPlace(String placeId, String url, String name, String formatted_address, float rating, double xLocation, double yLocation, String iconUrl) {
        places.add(new LodgingPlaceVO(placeId, url, name, formatted_address, rating, xLocation, yLocation, iconUrl));
    }
    public void addTourPlace(String placeId, String url, String name, String formatted_address, float rating, double xLocation, double yLocation, String iconUrl) {
        places.add(new TourPlaceVO(placeId, url, name, formatted_address, rating, xLocation, yLocation, iconUrl));
    }
    public void addRestaurantPlace(String placeId, String url, String name, String formatted_address, float rating, double xLocation, double yLocation, String iconUrl) {
        places.add(new RestaurantPlaceVO(placeId, url, name, formatted_address, rating, xLocation, yLocation, iconUrl));
    }
    public void addPlace(List<? extends PlaceVO> places) {
        this.places.addAll(places);
    }
}
