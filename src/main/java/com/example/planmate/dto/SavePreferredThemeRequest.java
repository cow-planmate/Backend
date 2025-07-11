package com.example.planmate.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class SavePreferredThemeRequest {
    private List<Integer> preferredThemeIds;
}
