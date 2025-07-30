package com.example.planmate.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
@Getter
@Setter
public class WebSocketChangeIdResponse {
    private String type;
    private String object;
    private Map<Integer, Integer> map;
}
