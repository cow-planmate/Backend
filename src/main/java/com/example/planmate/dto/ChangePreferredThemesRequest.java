package com.example.planmate.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChangePreferredThemesRequest implements IRequest {
    private int preferredThemeCategoryId;
    private List<Integer>  preferredThemeIds;

}
