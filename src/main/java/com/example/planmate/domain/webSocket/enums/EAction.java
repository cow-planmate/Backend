package com.example.planmate.domain.webSocket.enums;

public enum EAction {
    CREATE("create"),
    DELETE("delete");

    private final String value;

    EAction(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
