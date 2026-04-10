package com.example.planmate.domain.user.dto;

import com.example.planmate.common.dto.IRequest;
import com.example.planmate.common.valueObject.PreferredThemeUpdateVO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChangePreferredThemesRequest implements IRequest {
    private List<PreferredThemeUpdateVO> themeUpdates;

}
