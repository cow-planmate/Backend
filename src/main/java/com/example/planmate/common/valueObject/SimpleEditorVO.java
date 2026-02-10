package com.example.planmate.common.valueObject;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SimpleEditorVO {
    private UUID userId;
    private String nickName;
}
