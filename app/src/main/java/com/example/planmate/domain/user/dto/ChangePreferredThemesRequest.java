package com.example.planmate.domain.user.dto;

import com.example.planmate.common.dto.IRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChangePreferredThemesRequest implements IRequest {
    private int preferredThemeCategoryId;
    private List<Integer>  preferredThemeIds;

}
