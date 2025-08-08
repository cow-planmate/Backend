package com.example.planmate.domain.plan.dto;

import com.example.planmate.common.dto.CommonResponse;
import com.example.planmate.common.valueObject.SimpleEditorVO;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GetEditorsResponse extends CommonResponse {
    private List<SimpleEditorVO> simpleEditorVOs;

    public GetEditorsResponse(){
        simpleEditorVOs = new ArrayList<>();
    }

    public void addSimpleEditorVO(int userId, String nickName) {
        simpleEditorVOs.add(new SimpleEditorVO(userId, nickName));
    }
}

