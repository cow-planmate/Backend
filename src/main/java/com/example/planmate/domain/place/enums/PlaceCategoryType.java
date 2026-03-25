package com.example.planmate.domain.place.enums;

public enum PlaceCategoryType {
    TOUR(0),
    LODGING(1),
    RESTAURANT(2),
    SEARCH(4);

    private final int id;

    PlaceCategoryType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
