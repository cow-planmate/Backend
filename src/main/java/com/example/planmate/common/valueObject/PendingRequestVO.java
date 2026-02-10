package com.example.planmate.common.valueObject;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;


@Getter
@AllArgsConstructor
public class PendingRequestVO {
    private int requestId;
    private UUID senderId;
    private String senderNickname;
    private UUID planId;
    private String planName;
    private String type;
}
